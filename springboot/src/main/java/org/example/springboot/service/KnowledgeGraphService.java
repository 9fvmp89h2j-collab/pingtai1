package org.example.springboot.service;

import org.example.springboot.dto.response.KnowledgeGraphAcupointDTO;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeGraphService {

    private final ObjectProvider<Driver> driverProvider;

    @org.springframework.beans.factory.annotation.Value("${neo4j.database:neo4j}")
    private String database;

    public KnowledgeGraphService(ObjectProvider<Driver> driverProvider) {
        this.driverProvider = driverProvider;
    }

    public List<KnowledgeGraphAcupointDTO> listAcupoints() {
        Driver driver = driverProvider.getIfAvailable();
        if (driver == null) {
            return demoAcupoints();
        }

        String cypher = """
                MATCH (a:Acupoint)
                OPTIONAL MATCH (a)-[:BELONGS_TO]->(m:Meridian)
                OPTIONAL MATCH (a)-[:LOCATED_IN]->(r:BodyRegion)
                OPTIONAL MATCH (a)-[:HAS_SAFETY_TIP]->(s:SafetyTip)
                OPTIONAL MATCH (a)-[:CONFUSED_WITH]->(c:Acupoint)
                OPTIONAL MATCH (a)<-[:TARGETS]-(t:LearningTask)
                OPTIONAL MATCH (t)-[:REWARDS]->(rw:Reward)
                RETURN a, m.name AS meridian, r.name AS region, s.text AS safetyTip,
                       collect(DISTINCT c.name) AS confusedWith,
                       collect(DISTINCT t.name) AS tasks,
                       collect(DISTINCT rw.name) AS rewards
                ORDER BY a.sort, a.name
                """;

        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeRead(tx -> tx.run(cypher).list(this::toAcupoint));
        } catch (Exception ignored) {
            return demoAcupoints();
        }
    }

    public KnowledgeGraphAcupointDTO getAcupoint(String id) {
        return listAcupoints().stream()
                .filter(item -> item.getId().equalsIgnoreCase(id) || item.getName().equals(id))
                .findFirst()
                .orElse(null);
    }

    private KnowledgeGraphAcupointDTO toAcupoint(Record record) {
        Value node = record.get("a");
        Map<String, Object> data = node.asMap();
        return KnowledgeGraphAcupointDTO.builder()
                .id(String.valueOf(data.getOrDefault("id", data.getOrDefault("code", data.getOrDefault("name", "")))))
                .name(String.valueOf(data.getOrDefault("name", "")))
                .meridian(record.get("meridian").isNull() ? "" : record.get("meridian").asString())
                .region(record.get("region").isNull() ? "" : record.get("region").asString())
                .childLocation(String.valueOf(data.getOrDefault("childLocation", data.getOrDefault("description", ""))))
                .story(String.valueOf(data.getOrDefault("story", "")))
                .safetyTip(record.get("safetyTip").isNull() ? defaultSafetyTip() : record.get("safetyTip").asString())
                .confusedWith(toStringList(record.get("confusedWith")))
                .tasks(toStringList(record.get("tasks")))
                .rewards(toStringList(record.get("rewards")))
                .x(toDouble(data.get("x"), 0))
                .y(toDouble(data.get("y"), 1.2))
                .z(toDouble(data.get("z"), 0))
                .build();
    }

    private List<String> toStringList(Value value) {
        if (value == null || value.isNull()) return List.of();
        List<String> result = new ArrayList<>();
        value.asList(Value::asString).forEach(item -> {
            if (item != null && !item.isBlank()) result.add(item);
        });
        return result;
    }

    private Double toDouble(Object value, double fallback) {
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private String defaultSafetyTip() {
        return "本系统仅用于针灸文化科普和身体认知学习，不提供针刺操作或诊疗建议。";
    }

    private List<KnowledgeGraphAcupointDTO> demoAcupoints() {
        return List.of(
                KnowledgeGraphAcupointDTO.builder()
                        .id("hegu")
                        .name("合谷")
                        .meridian("手阳明大肠经")
                        .region("手部区域")
                        .childLocation("可以把它理解成小手地图上的重要星点，用来认识手背附近的位置。")
                        .story("小侦探在手部星图中找到第一枚线索星珠。")
                        .safetyTip("只用于位置认知和文化学习，不能用尖锐物品尝试针刺。")
                        .confusedWith(List.of("曲池"))
                        .tasks(List.of("找到手部星点", "阅读铜人线索"))
                        .rewards(List.of("穴位星珠", "铜片"))
                        .x(-0.78).y(0.35).z(0.08)
                        .build(),
                KnowledgeGraphAcupointDTO.builder()
                        .id("zusanli")
                        .name("足三里")
                        .meridian("足阳明胃经")
                        .region("小腿区域")
                        .childLocation("可以把它理解成小腿地图上的出发星，帮助认识膝盖下方的小腿方向。")
                        .story("星图罗盘提示小侦探观察小腿外侧的路线。")
                        .safetyTip("小腿不舒服要告诉家长或医生，不要自行按压或模仿治疗。")
                        .confusedWith(List.of("三阴交"))
                        .tasks(List.of("点亮小腿线索", "完成安全判断"))
                        .rewards(List.of("经络星砂", "安全铃铛"))
                        .x(0.34).y(-1.2).z(0.1)
                        .build(),
                KnowledgeGraphAcupointDTO.builder()
                        .id("baihui")
                        .name("百会")
                        .meridian("督脉")
                        .region("头面区域")
                        .childLocation("可以把它理解成头顶的小灯塔，用来认识头面区域。")
                        .story("小铜人老师请小侦探观察头顶星光。")
                        .safetyTip("头面区域不舒服时要告知大人，不能模仿任何治疗操作。")
                        .confusedWith(List.of())
                        .tasks(List.of("观察头顶星点"))
                        .rewards(List.of("铜片"))
                        .x(0.0).y(1.72).z(0.03)
                        .build(),
                KnowledgeGraphAcupointDTO.builder()
                        .id("zhongwan")
                        .name("中脘")
                        .meridian("任脉")
                        .region("腹部区域")
                        .childLocation("可以把它理解成身体中线上的小星点，帮助认识腹部区域。")
                        .story("侦探地图提示身体中线也藏着线索。")
                        .safetyTip("腹部不适应先休息并告知家长，不能自行处理。")
                        .confusedWith(List.of("神阙"))
                        .tasks(List.of("认识身体中线"))
                        .rewards(List.of("竹简碎片"))
                        .x(0.0).y(0.18).z(0.12)
                        .build(),
                KnowledgeGraphAcupointDTO.builder()
                        .id("sanyinjiao")
                        .name("三阴交")
                        .meridian("足太阴脾经")
                        .region("小腿区域")
                        .childLocation("可以把它理解成小腿内侧的星点，用来比较不同小腿星点的位置。")
                        .story("星光修补册会提醒小侦探区分小腿不同方向。")
                        .safetyTip("只观察示意位置，不进行针刺、按压或治疗模仿。")
                        .confusedWith(List.of("足三里"))
                        .tasks(List.of("区分小腿星点"))
                        .rewards(List.of("穴位星珠"))
                        .x(-0.22).y(-1.28).z(0.08)
                        .build()
        );
    }
}
