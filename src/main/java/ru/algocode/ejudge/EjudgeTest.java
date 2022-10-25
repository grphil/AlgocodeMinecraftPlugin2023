package ru.algocode.ejudge;

/**
 * @author Perveev Mike
 */
public class EjudgeTest {
    public static void main(String[] args) throws Exception {
        EjudgeSession session = new EjudgeSession("a-2021-minecraft-gribov", "tP3QLOY2Dy", 31101);
        session.authenticate();
        session.submit("A", "HUY");
        session.close();
    }
}
