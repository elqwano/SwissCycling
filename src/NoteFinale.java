public class NoteFinale {
    static double grade(int p1, int p2, int pE, double b) {
        double p2b = Math.ceil(130d * Math.pow(p2 / 130d, 1d / b));
        double rawGrade = 0.875 + 5.25 * ((p1 + p2b + pE) / 500d);
        return Math.rint(rawGrade * 4) / 4;
    }
    public static void main(String[] args) {
        int p1=11+15+17+17+13+69;
        int p2 = 92+16;
        int pE = 21+87;
//        int pE = 26+50; //val
//        int p1 = 140; //french
//        int p2 = 115;
//        int pE = 35 + 108;
        double b = 1;
        System.out.println(grade(p1,p2,pE,b));
    }
}
