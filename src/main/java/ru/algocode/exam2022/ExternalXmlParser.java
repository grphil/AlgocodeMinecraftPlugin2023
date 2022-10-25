package ru.algocode.exam2022;

import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;

public class ExternalXmlParser {

    private JavaPlugin plugin;
    private int lastRunId = 0;

    ExternalXmlParser(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void UpdateStandings(String externalXmlPath, HashMap<String, Stats> players) throws ParserConfigurationException, IOException, SAXException {
        Path externalLog;
        try {
            externalLog = Path.of(externalXmlPath);
        } catch (InvalidPathException e) {
            System.out.println("Can not open external xml");
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(externalLog.toFile());

        HashMap<Integer, Stats> playersById = new HashMap<>();

        for (Stats player : players.values()) {
            playersById.put(player.GetEjudgeId(), player);
        }

        NodeList runs = ((Element) document.getElementsByTagName("runs").item(0)).getElementsByTagName("run");

        for (int i = lastRunId; i < runs.getLength(); i++) {
            Element run = (Element) runs.item(i);

            if (run.getAttribute("prob_id").isEmpty() || run.getAttribute("user_id").isEmpty()) {
                continue;
            }

            int userId = Integer.parseInt(run.getAttribute("user_id"));
            if (!playersById.containsKey(userId)) {
                continue;
            }
            int probId = Integer.parseInt(run.getAttribute("prob_id"));
            probId--;

            String status = run.getAttribute("status");
            if (status.isEmpty()) {
                continue;
            }

            if (status.equals("RU") || status.equals("CG") || status.equals("CD") || status.equals("PD")) {
                break;
            }

            Stats player = playersById.get(userId);

            plugin.getServer().broadcastMessage("Player " + player.GetName() + " got " + status + " for problem " + probId);
            player.ChangeProblem(probId, status);
            lastRunId = i + 1;
        }
    }
}
