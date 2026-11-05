package com.dorohedoro.hole.character;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared deterministic personality-test rules. No Minecraft or client dependencies.
 * The CLIENT may preview scores, but the SERVER independently recomputes every result.
 */
public final class PersonalityQuiz {
    private PersonalityQuiz() {}
    public record Option(String text, MagicType primary, MagicType secondary) {}
    public record Question(String text, Option[] options) {}
    public record Score(MagicType recommendation, int[] points) {
        public int points(MagicType type) { return points[type.ordinal()]; }
    }
    private static Option o(String label, MagicType primary, MagicType secondary) {
        return new Option(label, primary, secondary);
    }
    private static final Question[] QUESTIONS = new Question[] {
        new Question("A stranger collapses outside your door. You...", new Option[] {
            o("Treat their injuries before asking questions.", MagicType.HEALING, MagicType.PROTECTION),
            o("Stand guard so nobody can hurt them.", MagicType.PROTECTION, MagicType.HEALING),
            o("Track the attacker and expose their weakness.", MagicType.DISMEMBERMENT, MagicType.CURSE),
            o("Make the attacker regret returning.", MagicType.CURSE, MagicType.DESTRUCTION)
        }),
        new Question("A sealed passage blocks your way. You...", new Option[] {
            o("Grow something through the cracks.", MagicType.MUSHROOM, MagicType.CURSE),
            o("Find a shape that can squeeze inside.", MagicType.REPTILE, MagicType.DISMEMBERMENT),
            o("Break down the obstruction.", MagicType.DESTRUCTION, MagicType.MUSHROOM),
            o("Study how it might have opened before.", MagicType.TIME, MagicType.PROTECTION)
        }),
        new Question("A creature lunges at your friend. You...", new Option[] {
            o("Create a barrier between them.", MagicType.PROTECTION, MagicType.HEALING),
            o("Punish it with a lasting affliction.", MagicType.CURSE, MagicType.TIME),
            o("Strike hard to end the threat quickly.", MagicType.DESTRUCTION, MagicType.DISMEMBERMENT),
            o("Disable its limbs with precision.", MagicType.DISMEMBERMENT, MagicType.PROTECTION)
        }),
        new Question("You could undo one terrible event. You...", new Option[] {
            o("Heal those who still suffer.", MagicType.HEALING, MagicType.PROTECTION),
            o("Turn back the critical moment.", MagicType.TIME, MagicType.CURSE),
            o("Protect others from a repeat.", MagicType.PROTECTION, MagicType.TIME),
            o("Let new life reclaim what was lost.", MagicType.MUSHROOM, MagicType.HEALING)
        }),
        new Question("You are trapped underground. You...", new Option[] {
            o("Adapt your body to find an escape route.", MagicType.REPTILE, MagicType.PROTECTION),
            o("Cultivate something that lights your path.", MagicType.MUSHROOM, MagicType.HEALING),
            o("Force open a new exit.", MagicType.DESTRUCTION, MagicType.REPTILE),
            o("Wait and study the rhythm of the tunnels.", MagicType.TIME, MagicType.DISMEMBERMENT)
        }),
        new Question("In a dangerous fight, you trust...", new Option[] {
            o("Clean, exact strikes.", MagicType.DISMEMBERMENT, MagicType.REPTILE),
            o("Overwhelming pressure.", MagicType.DESTRUCTION, MagicType.CURSE),
            o("A consequence your enemy cannot ignore.", MagicType.CURSE, MagicType.MUSHROOM),
            o("Staying safe until a chance appears.", MagicType.PROTECTION, MagicType.TIME)
        }),
        new Question("An unfamiliar sickness spreads. You...", new Option[] {
            o("Care for the sick and search for treatment.", MagicType.HEALING, MagicType.MUSHROOM),
            o("Study what is growing inside the victims.", MagicType.MUSHROOM, MagicType.HEALING),
            o("Trace the earliest cases and their timing.", MagicType.TIME, MagicType.DISMEMBERMENT),
            o("Find the source and seal it away.", MagicType.CURSE, MagicType.PROTECTION)
        }),
        new Question("You are offered a new body. You...", new Option[] {
            o("Accept a stronger, stranger form.", MagicType.REPTILE, MagicType.DESTRUCTION),
            o("Choose precise control over every movement.", MagicType.DISMEMBERMENT, MagicType.REPTILE),
            o("Keep your body and gain a protective shell.", MagicType.PROTECTION, MagicType.HEALING),
            o("Restore your body to its healthiest state.", MagicType.HEALING, MagicType.TIME)
        }),
        new Question("Black smoke gathers around your hands. You...", new Option[] {
            o("Shape it into something that can spread.", MagicType.MUSHROOM, MagicType.CURSE),
            o("Let it reshape you.", MagicType.REPTILE, MagicType.HEALING),
            o("Launch it forward as a weapon.", MagicType.DESTRUCTION, MagicType.DISMEMBERMENT),
            o("Try to suspend one moment within it.", MagicType.TIME, MagicType.PROTECTION)
        }),
        new Question("Someone threatens the people you love. You...", new Option[] {
            o("Shield them, even if you must stand alone.", MagicType.PROTECTION, MagicType.REPTILE),
            o("Restore anyone who is hurt.", MagicType.HEALING, MagicType.MUSHROOM),
            o("Make the threat carry a terrible mark.", MagicType.CURSE, MagicType.DESTRUCTION),
            o("End the threat with exact force.", MagicType.DISMEMBERMENT, MagicType.TIME)
        }),
        new Question("In an abandoned factory, you notice...", new Option[] {
            o("Fungi growing in the damp machinery.", MagicType.MUSHROOM, MagicType.HEALING),
            o("A machine that could change your body.", MagicType.REPTILE, MagicType.DISMEMBERMENT),
            o("The energy still trapped in its engines.", MagicType.DESTRUCTION, MagicType.PROTECTION),
            o("Clocks frozen at the same instant.", MagicType.TIME, MagicType.CURSE)
        }),
        new Question("What kind of mastery appeals to you?", new Option[] {
            o("Understanding where to separate and repair.", MagicType.DISMEMBERMENT, MagicType.HEALING),
            o("The ability to rewrite a lasting consequence.", MagicType.CURSE, MagicType.TIME),
            o("The discipline to protect without retreat.", MagicType.PROTECTION, MagicType.DESTRUCTION),
            o("The power to heal what others cannot.", MagicType.HEALING, MagicType.MUSHROOM)
        })
    };
    public static int count() { return QUESTIONS.length; }
    public static Question question(int i) { return QUESTIONS[i]; }

    public static final int QUESTIONS_PER_RUN = 8;

    /** Server chooses eight distinct questions in unpredictable order. */
    public static int[] randomQuestions(Random serverRandom) {
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < QUESTIONS.length; i++) pool.add(i);
        Collections.shuffle(pool, serverRandom);
        int[] order = new int[QUESTIONS_PER_RUN];
        for (int i = 0; i < order.length; i++) order[i] = pool.get(i);
        return order;
    }

    public static String encodeOrder(int[] order) {
        StringBuilder out = new StringBuilder();
        for (int i=0;i<order.length;i++) {
            if (i>0) out.append(',');
            out.append(order[i]);
        }
        return out.toString();
    }

    public static int[] parseOrder(String value) {
        if (value == null) return null;
        String[] parts = value.split(",", -1);
        if (parts.length != QUESTIONS_PER_RUN) return null;
        int[] order = new int[QUESTIONS_PER_RUN];
        boolean[] seen = new boolean[QUESTIONS.length];
        try {
            for(int i=0;i<parts.length;i++) {
                int n = Integer.parseInt(parts[i]);
                if (n<0 || n>=QUESTIONS.length || seen[n]) return null;
                seen[n] = true;order[i]=n;
            }
        } catch(NumberFormatException ex) {return null;}
        return order;
    }

    /** Only the eight questions actually displayed are submitted. */
    public static boolean valid(int[] order, String answers) {
        if (order == null || order.length != QUESTIONS_PER_RUN || answers == null || answers.length() != order.length) return false;
        if (parseOrder(encodeOrder(order)) == null) return false;
        for (int i=0;i<answers.length();i++) if(answers.charAt(i)<'0' || answers.charAt(i)>'3') return false;
        return true;
    }

    public static Score score(int[] order, String answers) {
        if (!valid(order, answers)) throw new IllegalArgumentException("Invalid quiz answers or question order");
        int[] tally = new int[MagicType.values().length];
        for (int i=0;i<order.length;i++) {
            Option choice = QUESTIONS[order[i]].options()[answers.charAt(i)-'0'];
            tally[choice.primary().ordinal()] += 3;
            tally[choice.secondary().ordinal()] += 1;
        }
        int best = -1;
        MagicType winner = null;
        for (MagicType type:MagicType.values()) {
            if(tally[type.ordinal()]>best) {best=tally[type.ordinal()]; winner=type;}
        }
        return new Score(winner,Arrays.copyOf(tally,tally.length));
    }

    /** The only selectable quiz outcomes are tied for highest score; no free choice. */
    public static MagicType[] leaders(int[] order,String answers) {
        Score score=score(order,answers);
        int best=score.points(score.recommendation());
        List<MagicType> winners=new ArrayList<>();
        for(MagicType type:MagicType.values())
            if(score.points(type)==best) winners.add(type);
        return winners.toArray(new MagicType[0]);
    }

    /** The optional independent fate roll ignores quiz scoring; executed server-side only. */
    public static MagicType rollFate(Random rng) {
        int sum=0;
        for(MagicType type:MagicType.values()) sum+=type.fateWeight();
        int value=rng.nextInt(sum);
        for(MagicType type:MagicType.values()) {
            value-=type.fateWeight();
            if(value<0) return type;
        }
        throw new IllegalStateException("No fate outcome");
    }
}
