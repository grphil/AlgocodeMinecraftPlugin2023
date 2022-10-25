package ru.algocode.exam2022;

import java.util.*;

public class Stats {

    private String name;
    private String login;

    private String ejudgeLogin;
    private String ejudgePassword;
    private int ejudgeId;

    private int totalScore;

    private int problemScore;
    private int inGame;
    private int kills;
    private int cruelKills;
    private int deaths;
    private int purchased;

    private int bonus;

    private int penalty;

    private int timeUntilNextKill;
    private int timeInForbiddenZone;

    private boolean inForbiddenZone;
    private List<String> problems;

    Stats(List<Object> sheetsRow) {
        this.name = (String) sheetsRow.get(0);
        this.login = (String) sheetsRow.get(1);
        this.totalScore = Integer.parseInt((String) sheetsRow.get(2));
        this.problemScore = Integer.parseInt((String) sheetsRow.get(3));
        this.inGame = Integer.parseInt((String) sheetsRow.get(4));
        this.kills = Integer.parseInt((String) sheetsRow.get(5));
        this.cruelKills = Integer.parseInt((String) sheetsRow.get(6));
        this.deaths = Integer.parseInt((String) sheetsRow.get(7));
        this.purchased = Integer.parseInt((String) sheetsRow.get(8));
        this.bonus = Integer.parseInt((String) sheetsRow.get(9));
        this.penalty = Integer.parseInt((String) sheetsRow.get(10));
        this.timeUntilNextKill = Integer.parseInt((String) sheetsRow.get(11));
        this.timeInForbiddenZone = Integer.parseInt((String) sheetsRow.get(12));
        this.inForbiddenZone = false;
    }

    void LoadProblems(List<Object> problemsRow) {
        this.problems = new ArrayList<String>();
        for (Object problemVerdict : problemsRow) {
            this.problems.add((String) problemVerdict);
        }
    }

    void AddEjudgeAuth(List<Object> sheetsRow) {
        this.ejudgeLogin = (String) sheetsRow.get(2);
        this.ejudgePassword = (String) sheetsRow.get(3);
        this.ejudgeId =  Integer.parseInt((String) sheetsRow.get(4));
    }

    void ExportStats(List<Object> sheetsRow, List<Object> problemsRow) {
        sheetsRow.add(this.totalScore);
        sheetsRow.add(this.problemScore);
        sheetsRow.add(this.inGame);
        sheetsRow.add(this.kills);
        sheetsRow.add(this.cruelKills);
        sheetsRow.add(this.deaths);
        sheetsRow.add(this.purchased);
        sheetsRow.add(this.bonus);
        sheetsRow.add(this.penalty);
        sheetsRow.add(this.timeUntilNextKill);
        sheetsRow.add(this.timeInForbiddenZone);

        problemsRow.addAll(this.problems);
    }

    void RecalculateScore(Stats multipliers) {
        this.problemScore = 0;
        for (String verdict : this.problems) {
            if (Objects.equals(verdict, "OK")) {
                this.problemScore += 1;
            }
        }

        this.totalScore =
                this.problemScore * multipliers.problemScore +
                this.inGame * multipliers.inGame +
                this.kills * multipliers.kills +
                this.cruelKills * multipliers.cruelKills +
                this.deaths * multipliers.deaths +
                this.purchased * multipliers.purchased +
                this.bonus * multipliers.bonus +
                this.penalty * multipliers.penalty;
    }

    int GetScore() {
        return this.totalScore;
    }

    void Purchase(int price) {
        this.purchased += price;
    }

    void Died() {
        this.deaths++;
    }

    void KilledSomeone(int timeUntilNextKill) {
        this.kills++;
        this.timeUntilNextKill += timeUntilNextKill;
    }

    void CruelKilledSomeone(int timeUntilNextKill) {
        this.cruelKills++;
        this.timeUntilNextKill += timeUntilNextKill;
    }

    void IncInGame() {
        this.inGame++;
    }

    void Tick() {
        if (this.timeUntilNextKill > 0) {
            this.timeUntilNextKill--;
        }
    }

    void InAllowedZone() {
        if (this.timeInForbiddenZone > 0) {
            this.timeInForbiddenZone--;
        }
        this.inForbiddenZone = false;
    }

    void InForbiddenZone(int toAdd) {
        this.timeInForbiddenZone += toAdd;
        this.inForbiddenZone = true;
    }

    void TooLongInForbiddenZone(int penalty) {
        this.penalty += penalty;
        this.timeInForbiddenZone = 0;
        this.inForbiddenZone = false;
    }

    int GetTimeInForbiddenZone() {
        return this.timeInForbiddenZone;
    }

    boolean GetIsInForbiddenZone() {
        return this.inForbiddenZone;
    }

    int GetTimeUntilNextKill() {
        return this.timeUntilNextKill;
    }

    void SetBonus(int bonus) {
        this.bonus = bonus;
    }

    void ChangeProblem(int problemId, String verdict) {
        while (this.problems.size() <= problemId) {
            this.problems.add("");
        }
        if (problemId >= 0) {
            this.problems.set(problemId, verdict);
        }
    }

    String GetName() {
        return this.name;
    }

    String GetLogin() {
        return this.login;
    }

    String GetEjudgeLogin() {
        return this.ejudgeLogin;
    }

    String GetEjudgePassword() {
        return this.ejudgePassword;
    }

    int GetEjudgeId() {
        return this.ejudgeId;
    }

    String GetStatus() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < problems.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            if (problems.get(i).isEmpty()) {
                result.append("NO");
            } else {
                result.append(problems.get(i));
            }
        }
        return result.toString();
    }
}
