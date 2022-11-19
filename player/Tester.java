package player;

public class Tester {
    public static void main(String[] args) {
        System.out.println("MNKPlayerTester tests\n");
        configurationTest(3, 3, 3); // patta
        configurationTest(4, 3, 3); // vittoria
        configurationTest(4, 4, 3); // vittoria
        configurationTest(4, 4, 4); // patta
        configurationTest(5, 4, 4); // patta
        configurationTest(5, 5, 4); // patta
        configurationTest(5, 5, 5); // patta
        configurationTest(6, 4, 4); // patta
        configurationTest(6, 5, 4); // vittoria
        configurationTest(6, 6, 4); // vittoria
        configurationTest(6, 6, 5); // patta
        configurationTest(6, 6, 6); // patta
        configurationTest(7, 4, 4); // patta
        configurationTest(7, 5, 4); // vittoria
        configurationTest(7, 6, 4); // vittoria
        configurationTest(7, 7, 4); // vittoria
        configurationTest(7, 5, 5); // patta
        configurationTest(7, 6, 5); // patta
        configurationTest(7, 7, 5); // patta
        configurationTest(7, 7, 6); // patta
        configurationTest(7, 7, 7); // ?
        configurationTest(8, 8, 4); // vittoria
        configurationTest(10, 10, 5); // ?
        configurationTest(50, 50, 10); // ?
        configurationTest(70, 70, 10); // ?
    }

    protected static void configurationTest(int m, int n, int k) {
        if (m <= 0 || n <= 0 || k <= 0)
            throw new IllegalArgumentException("At least one of the arguments is not strictly positive.");
        System.out.println("\n" + m + "," + n + "," + k + "-game");
        final String valueOfM = String.valueOf(m), valueOfN = String.valueOf(n), valueOfK = String.valueOf(k);
        mnkgame.MNKPlayerTester.main(
                new String[] { "-r", "2", valueOfM, valueOfN, valueOfK, "player.NostroPlayer", "monkey.MoNKey" });
        mnkgame.MNKPlayerTester.main(
                new String[] { "-r", "2", valueOfM, valueOfN, valueOfK, "monkey.MoNKey", "player.NostroPlayer" });
    }

}
