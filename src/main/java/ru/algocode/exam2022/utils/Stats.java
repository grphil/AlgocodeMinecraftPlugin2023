package ru.algocode.exam2022.utils;

import java.util.*;

public class Stats {

    final private String name;
    final private String login;

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

    public Stats(List<Object> sheetsRow) {
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

    public void LoadProblems(List<Object> problemsRow) {
        this.problems = new ArrayList<>();
        for (Object problemVerdict : problemsRow) {
            this.problems.add((String) problemVerdict);
        }
    }

    public void AddEjudgeAuth(List<Object> sheetsRow) {
        this.ejudgeLogin = (String) sheetsRow.get(2);
        this.ejudgePassword = (String) sheetsRow.get(3);
        this.ejudgeId =  Integer.parseInt((String) sheetsRow.get(4));
    }

    public void ExportStats(List<Object> sheetsRow, List<Object> problemsRow) {
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

    public void RecalculateScore(Stats multipliers) {
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

    public int GetScore() {
        return this.totalScore;
    }

    public void Purchase(int price) {
        this.purchased += price;
    }

    public void Died() {
        this.deaths++;
    }

    public void KilledSomeone(int timeUntilNextKill) {
        this.kills++;
        this.timeUntilNextKill += timeUntilNextKill;
    }

    public void CruelKilledSomeone(int timeUntilNextKill) {
        this.cruelKills++;
        this.timeUntilNextKill += timeUntilNextKill;
    }

    public void IncInGame() {
        this.inGame++;
    }

    public void Tick() {
        if (this.timeUntilNextKill > 0) {
            this.timeUntilNextKill--;
        }
    }

    public void InAllowedZone() {
        if (this.timeInForbiddenZone > 0) {
            this.timeInForbiddenZone--;
        }
        this.inForbiddenZone = false;
    }

    public void InForbiddenZone(int toAdd) {
        this.timeInForbiddenZone += toAdd;
        this.inForbiddenZone = true;
    }

    public void TooLongInForbiddenZone(int penalty) {
        this.penalty += penalty;
        this.timeInForbiddenZone = 0;
        this.inForbiddenZone = false;
    }

    public int GetTimeInForbiddenZone() {
        return this.timeInForbiddenZone;
    }

    public boolean GetIsInForbiddenZone() {
        return this.inForbiddenZone;
    }

    public int GetTimeUntilNextKill() {
        return this.timeUntilNextKill;
    }

    public void SetBonus(int bonus) {
        this.bonus = bonus;
    }

    public void ChangeProblem(int problemId, String verdict) {
        while (this.problems.size() <= problemId) {
            this.problems.add("");
        }
        if (problemId >= 0) {
            this.problems.set(problemId, verdict);
        }
    }

    public String GetName() {
        return this.name;
    }

    public String GetLogin() {
        return this.login;
    }

    public String GetEjudgeLogin() {
        return this.ejudgeLogin;
    }

    public String GetEjudgePassword() {
        return this.ejudgePassword;
    }

    public int GetEjudgeId() {
        return this.ejudgeId;
    }

    public String GetStatus() {
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
