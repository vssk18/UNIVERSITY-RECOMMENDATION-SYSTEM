package logic;

import model.University;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationEngine {

    // ------------ Enums ------------

    public enum Mode {
        PREDICT,
        VIEW_ALL
    }

    public enum ProfileTier {
        EXCEPTIONAL,
        VERY_STRONG,
        STRONG,
        GOOD,
        DEVELOPING,
        BASIC
    }

    public enum Bucket {
        AMBITIOUS,
        TARGET,
        SAFE
    }

    // ------------ Helper types ------------

    public static final class ScoredUniversity {
        public final University uni;
        public final double fitScore;
        public final Bucket bucket;
        public final List<String> reasons;

        public ScoredUniversity(University uni, double fitScore, Bucket bucket, List<String> reasons) {
            this.uni = uni;
            this.fitScore = fitScore;
            this.bucket = bucket;
            this.reasons = reasons;
        }
    }

    public static final class Result {
        public final Mode mode;
        public final double profileScore;        // 0–100
        public final ProfileTier tier;
        public final int totalUniversities;
        public final int excludedByIelts;
        public final List<ScoredUniversity> ambitious;
        public final List<ScoredUniversity> target;
        public final List<ScoredUniversity> safe;
        public final LocalDateTime generatedAt;

        public Result(Mode mode,
                      double profileScore,
                      ProfileTier tier,
                      int totalUniversities,
                      int excludedByIelts,
                      List<ScoredUniversity> ambitious,
                      List<ScoredUniversity> target,
                      List<ScoredUniversity> safe,
                      LocalDateTime generatedAt) {
            this.mode = mode;
            this.profileScore = profileScore;
            this.tier = tier;
            this.totalUniversities = totalUniversities;
            this.excludedByIelts = excludedByIelts;
            this.ambitious = ambitious;
            this.target = target;
            this.safe = safe;
            this.generatedAt = generatedAt;
        }
    }

    // Comparator used everywhere for ordering within a bucket
    private static final Comparator<ScoredUniversity> BY_RANK_THEN_NAME =
            Comparator.comparingInt((ScoredUniversity s) -> safeGlobalRank(s.uni))
                      .thenComparing(s -> s.uni.getName());

    // ------------ Public entry point ------------

    public static Result recommend(List<University> all,
                                   EligibilityProfile profile,
                                   Mode mode,
                                   int desiredCount) {

        // 1) Compute profile strength
        double profileScore = profile.computeProfileScore();
        ProfileTier tier = classifyTier(profileScore);

        // 2) Filter by course type + region
        List<University> matching = all.stream()
                .filter(u -> u.getCourseType() == profile.getCourseType())
                .filter(u -> profile.getRegions().contains(u.getRegion()))
                .collect(Collectors.toList());

        int total = matching.size();

        // 3) IELTS + budget filtering (hard constraints), track IELTS exclusions
        List<University> eligible = new ArrayList<>();
        int excludedByIelts = 0;

        for (University u : matching) {
            double minIelts = u.getMinIelts();
            // Strict IELTS filter: if user < program minimum -> excluded
            if (profile.getIeltsOverall() + 1e-9 < minIelts) {
                excludedByIelts++;
                continue;
            }
            // Budget filter (0 = no limit)
            if (profile.getBudgetUsd() > 0 && u.getEstimatedTotalUsd() > profile.getBudgetUsd()) {
                continue;
            }
            eligible.add(u);
        }

        if (eligible.isEmpty()) {
            // No matches after hard filters
            return new Result(
                    mode,
                    profileScore,
                    tier,
                    total,
                    excludedByIelts,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    LocalDateTime.now()
            );
        }

        // 4) Bucket each eligible university (Ambitious / Target / Safe)
        List<ScoredUniversity> ambitious = new ArrayList<>();
        List<ScoredUniversity> target = new ArrayList<>();
        List<ScoredUniversity> safe = new ArrayList<>();

        for (University u : eligible) {
            Bucket bucket = classifyBucket(u, tier);
            double fit = computeFitScore(u, profile);
            List<String> reasons = buildReasons(u, bucket);
            ScoredUniversity s = new ScoredUniversity(u, fit, bucket, reasons);
            switch (bucket) {
                case AMBITIOUS -> ambitious.add(s);
                case TARGET -> target.add(s);
                case SAFE -> safe.add(s);
            }
        }

        // 5) Sort within each bucket by global rank, then name
        ambitious.sort(BY_RANK_THEN_NAME);
        target.sort(BY_RANK_THEN_NAME);
        safe.sort(BY_RANK_THEN_NAME);

        // VIEW_ALL mode: return everything
        if (mode == Mode.VIEW_ALL) {
            return new Result(
                    mode,
                    profileScore,
                    tier,
                    total,
                    excludedByIelts,
                    ambitious,
                    target,
                    safe,
                    LocalDateTime.now()
            );
        }

        // 6) PREDICT mode: respect desiredCount with dynamic split
        int available = ambitious.size() + target.size() + safe.size();
        int N = desiredCount <= 0 ? available : Math.min(desiredCount, available);

        int[] quotas = computeQuotas(N); // [amb, tgt, safe]

        List<ScoredUniversity> finalAmb = new ArrayList<>();
        List<ScoredUniversity> finalTgt = new ArrayList<>();
        List<ScoredUniversity> finalSafe = new ArrayList<>();

        // Take per-bucket up to quota
        takeUpTo(ambitious, quotas[0], finalAmb);
        takeUpTo(target, quotas[1], finalTgt);
        takeUpTo(safe, quotas[2], finalSafe);

        // Track what's already picked
        Set<University> picked = new HashSet<>();
        finalAmb.forEach(s -> picked.add(s.uni));
        finalTgt.forEach(s -> picked.add(s.uni));
        finalSafe.forEach(s -> picked.add(s.uni));

        int pickedCount = finalAmb.size() + finalTgt.size() + finalSafe.size();

        // 7) If we still have fewer than N, top-up from remaining (best-ranked across all buckets)
        if (pickedCount < N) {
            List<ScoredUniversity> remaining = new ArrayList<>();
            for (ScoredUniversity s : ambitious) {
                if (!picked.contains(s.uni)) remaining.add(s);
            }
            for (ScoredUniversity s : target) {
                if (!picked.contains(s.uni)) remaining.add(s);
            }
            for (ScoredUniversity s : safe) {
                if (!picked.contains(s.uni)) remaining.add(s);
            }
            remaining.sort(BY_RANK_THEN_NAME);

            for (ScoredUniversity s : remaining) {
                if (pickedCount >= N) break;
                switch (s.bucket) {
                    case AMBITIOUS -> finalAmb.add(s);
                    case TARGET -> finalTgt.add(s);
                    case SAFE -> finalSafe.add(s);
                }
                picked.add(s.uni);
                pickedCount++;
            }
        }

        // 8) If we somehow exceeded N (due to rounding), trim in Safe → Target → Ambitious order
        pickedCount = finalAmb.size() + finalTgt.size() + finalSafe.size();
        if (pickedCount > N) {
            int excess = pickedCount - N;
            excess = trimList(finalSafe, excess);
            if (excess > 0) excess = trimList(finalTgt, excess);
            if (excess > 0) trimList(finalAmb, excess);
        }

        return new Result(
                mode,
                profileScore,
                tier,
                total,
                excludedByIelts,
                finalAmb,
                finalTgt,
                finalSafe,
                LocalDateTime.now()
        );
    }

    // ------------ Profile logic ------------

    private static ProfileTier classifyTier(double score) {
        if (score >= 90.0) return ProfileTier.EXCEPTIONAL;
        if (score >= 80.0) return ProfileTier.VERY_STRONG;
        if (score >= 70.0) return ProfileTier.STRONG;
        if (score >= 60.0) return ProfileTier.GOOD;
        if (score >= 45.0) return ProfileTier.DEVELOPING;
        return ProfileTier.BASIC;
    }

    // ------------ Bucket logic ------------

    private static Bucket classifyBucket(University u, ProfileTier tier) {
        int r = safeGlobalRank(u);
        if (r <= 0) {
            // Unknown rank → treat as SAFE
            return Bucket.SAFE;
        }

        // Hard rule: global top-15 are ALWAYS Ambitious
        if (r <= 15) {
            return Bucket.AMBITIOUS;
        }

        // Two boundaries per tier:
        //  r <= b1 → Ambitious
        //  r <= b2 → Target
        //  else    → Safe
        int b1;
        int b2;
        switch (tier) {
            case EXCEPTIONAL -> {
                b1 = 20;
                b2 = 80;
            }
            case VERY_STRONG -> {
                b1 = 50;
                b2 = 150;
            }
            case STRONG -> {
                b1 = 80;
                b2 = 200;
            }
            case GOOD -> {
                b1 = 150;
                b2 = 300;
            }
            case DEVELOPING -> {
                b1 = 250;
                b2 = 500;
            }
            case BASIC -> {
                b1 = 400;
                b2 = 800;
            }
            default -> {
                b1 = 250;
                b2 = 500;
            }
        }

        if (r <= b1) return Bucket.AMBITIOUS;
        if (r <= b2) return Bucket.TARGET;
        return Bucket.SAFE;
    }

    private static int safeGlobalRank(University u) {
        int r = u.getGlobalRank();
        if (r <= 0) return 9999;
        return r;
    }

    // ------------ Fit score (for tie-breaking / future use) ------------

    private static double computeFitScore(University u, EligibilityProfile p) {
        // Lower global rank is better → higher score
        int r = safeGlobalRank(u);
        double base = -r;

        double bonus = 0.0;
        if (u.hasResearchLab()) bonus += 5.0;
        if (u.isOnCampus()) bonus += 2.0;

        // You can later add profile-based bonuses here (e.g., match with research-heavy programs)
        return base + bonus;
    }

    private static List<String> buildReasons(University u, Bucket bucket) {
        List<String> reasons = new ArrayList<>();
        int r = u.getGlobalRank();
        if (r > 0) {
            reasons.add("global rank " + r);
        }
        if (u.hasResearchLab()) {
            reasons.add("active research lab");
        }
        if (u.isOnCampus()) {
            reasons.add("on-campus program");
        }
        reasons.add(bucket.name().toLowerCase() + " bucket by profile & rank");
        return reasons;
    }

    // ------------ Distribution logic ------------

    /**
     * Compute ambitious / target / safe quotas for N universities.
     *
     * If N <= 5 : 20% A, 60% T, 20% S
     * If N <= 10: 30% A, 50% T, 20% S
     * If N <= 15: 25% A, 50% T, 25% S
     * If N > 15 : 20% A, 40% T, 40% S
     */
    private static int[] computeQuotas(int N) {
        double pa, pt, ps;
        if (N <= 5) {
            pa = 0.20; pt = 0.60; ps = 0.20;
        } else if (N <= 10) {
            pa = 0.30; pt = 0.50; ps = 0.20;
        } else if (N <= 15) {
            pa = 0.25; pt = 0.50; ps = 0.25;
        } else {
            pa = 0.20; pt = 0.40; ps = 0.40;
        }

        int a = (int) Math.round(N * pa);
        int t = (int) Math.round(N * pt);
        int s = (int) Math.round(N * ps);

        int sum = a + t + s;

        // Adjust to ensure a + t + s == N
        while (sum < N) {
            // Give extra to Target by default, then Safe, then Ambitious
            if (t <= s && t <= a) t++;
            else if (s <= a) s++;
            else a++;
            sum = a + t + s;
        }
        while (sum > N) {
            // Remove from Safe first, then Target, then Ambitious
            if (s > 0) s--;
            else if (t > 0) t--;
            else if (a > 0) a--;
            sum = a + t + s;
        }

        return new int[]{a, t, s};
    }

    private static void takeUpTo(List<ScoredUniversity> src, int count, List<ScoredUniversity> dest) {
        for (int i = 0; i < src.size() && dest.size() < count; i++) {
            dest.add(src.get(i));
        }
    }

    /**
     * Trim up to `excess` entries from the end of the list.
     *
     * @return remaining excess after trimming this list.
     */
    private static int trimList(List<ScoredUniversity> list, int excess) {
        int canTrim = Math.min(excess, list.size());
        for (int i = 0; i < canTrim; i++) {
            list.remove(list.size() - 1);
        }
        return excess - canTrim;
    }
}