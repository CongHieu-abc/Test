package com.swp.myleague.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.service.informationservice.ClubService;

import jakarta.servlet.http.HttpSession;

// Simple fake classes to avoid Mockito inline mocking issues on newer JDKs
class FakeMatchService extends MatchService {
    private List<Match> fixturesToReturn;
    private List<Match> savedMatches;
    private List<Match> allMatchesToReturn;
    private Integer lastRoundNumber;

    public FakeMatchService() {
        // MatchService uses @Autowired, so we need to set fields after construction
        // We'll use ReflectionTestUtils in setup() to initialize dependencies
    }
    
    // Initialize dependencies to avoid NullPointerException
    void initialize() {
        org.springframework.test.util.ReflectionTestUtils.setField(this, "matchRepo", null);
        org.springframework.test.util.ReflectionTestUtils.setField(this, "clubRepo", null);
        org.springframework.test.util.ReflectionTestUtils.setField(this, "matchClubStatRepo", null);
        this.allMatchesToReturn = new ArrayList<>(); // Default to empty list
    }

    void setFixturesToReturn(List<Match> fixtures) {
        this.fixturesToReturn = fixtures;
    }

    void setAllMatchesToReturn(List<Match> matches) {
        this.allMatchesToReturn = matches != null ? new ArrayList<>(matches) : new ArrayList<>();
    }

    @Override
    public List<Match> getAll() {
        return new ArrayList<>(allMatchesToReturn);
    }

    @Override
    public List<Match> autoGenFixturesMatches(java.time.LocalDate startDate, List<java.time.LocalTime> matchSlots) {
        return fixturesToReturn != null ? new ArrayList<>(fixturesToReturn) : new ArrayList<>();
    }

    @Override
    public List<Match> saveAuto(List<Match> matches) {
        this.savedMatches = new ArrayList<>(matches);
        this.lastRoundNumber = null; // Will be set by controller
        return new ArrayList<>(matches);
    }

    List<Match> getSavedMatches() {
        return savedMatches;
    }

    Integer getLastRoundNumber() {
        return lastRoundNumber;
    }
}

class FakeClubService extends ClubService {
    private List<Club> clubsToReturn;

    public FakeClubService() {
        // ClubService uses @Autowired, so we need to set fields after construction
    }

    void initialize() {
        org.springframework.test.util.ReflectionTestUtils.setField(this, "clubRepo", null);
        this.clubsToReturn = new ArrayList<>(); // Default to empty list
    }

    void setClubsToReturn(List<Club> clubs) {
        this.clubsToReturn = clubs != null ? new ArrayList<>(clubs) : new ArrayList<>();
    }

    @Override
    public List<Club> getAll() {
        return new ArrayList<>(clubsToReturn);
    }
}

public class AdminControllerFixtureTest {

    private AdminController controller;
    private FakeMatchService matchService;
    private FakeClubService clubService;
    private HttpSession session;
    private Model model;

    @BeforeEach
    void setup() {
        controller = new AdminController();
        matchService = new FakeMatchService();
        matchService.initialize(); // Initialize dependencies
        clubService = new FakeClubService();
        clubService.initialize(); // Initialize dependencies
        
        // Set up default clubs for tests
        List<Club> defaultClubs = createDefaultClubs();
        clubService.setClubsToReturn(defaultClubs);
        
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        // Inject fake services into controller
        ReflectionTestUtils.setField(controller, "matchService", matchService);
        ReflectionTestUtils.setField(controller, "clubService", clubService);
    }

    // ========== TEST CASE 1: Tạo fixtures mới khi không có session ==========
    @Test
    @DisplayName("TC1: Tạo fixtures mới khi session trống")
    void testGetAddFixtures_NoSession_CreatesNewFixtures() {
        // Given: Không có fixtures trong session, isRecreate = false
        String startDateStr = "2025-01-01";
        Boolean isRecreate = false;
        List<Match> expectedFixtures = createSampleFixtures();

        matchService.setFixturesToReturn(expectedFixtures);

        // When
        String result = controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then
        assertEquals("redirect:/admin", result);
        assertNotNull(session.getAttribute("autoFixturesMatch"), "Session nên có fixtures");
        assertEquals(true, model.getAttribute("hasAutoFixtureSession"), "Model nên có hasAutoFixtureSession = true");
        
        @SuppressWarnings("unchecked")
        List<Match> sessionFixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        assertEquals(expectedFixtures.size(), sessionFixtures.size(), "Session fixtures nên có đúng số lượng");
        
        assertNotNull(model.getAttribute("fixtures"), "Model nên có fixtures");
        assertNotNull(model.getAttribute("fixturesByRound"), "Model nên có fixturesByRound");
    }

    // ========== TEST CASE 2: Sử dụng fixtures từ session khi không recreate ==========
    @Test
    @DisplayName("TC2: Sử dụng fixtures từ session khi isRecreate = false")
    void testGetAddFixtures_WithSession_NoRecreate_UsesSessionFixtures() {
        // Given: Có fixtures trong session, isRecreate = false
        List<Match> sessionFixtures = createSampleFixtures();
        session.setAttribute("autoFixturesMatch", sessionFixtures);
        String startDateStr = "2025-01-01";
        Boolean isRecreate = false;

        // When
        String result = controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: Không gọi matchService.autoGenFixturesMatches
        assertEquals("redirect:/admin", result);
        @SuppressWarnings("unchecked")
        List<Match> resultFixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        assertEquals(sessionFixtures.size(), resultFixtures.size(), "Nên sử dụng fixtures từ session");
    }

    // ========== TEST CASE 3: Tạo lại fixtures khi isRecreate = true ==========
    @Test
    @DisplayName("TC3: Tạo lại fixtures khi isRecreate = true")
    void testGetAddFixtures_WithSession_Recreate_CreatesNewFixtures() {
        // Given: Có fixtures cũ trong session, isRecreate = true
        List<Match> oldFixtures = createSampleFixtures();
        session.setAttribute("autoFixturesMatch", oldFixtures);
        
        List<Match> newFixtures = createSampleFixtures();
        // Thay đổi một chút để phân biệt
        newFixtures.get(0).setMatchTitle("New Match Title");
        matchService.setFixturesToReturn(newFixtures);
        
        String startDateStr = "2025-01-01";
        Boolean isRecreate = true;

        // When
        String result = controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: Nên tạo fixtures mới
        assertEquals("redirect:/admin", result);
        @SuppressWarnings("unchecked")
        List<Match> resultFixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        assertEquals("New Match Title", resultFixtures.get(0).getMatchTitle(), "Nên có fixtures mới");
    }

    // ========== TEST CASE 4: Kiểm tra fixturesByRound được group đúng ==========
    @Test
    @DisplayName("TC4: Fixtures được group theo round đúng cách")
    void testGetAddFixtures_FixturesGroupedByRound() {
        // Given: Fixtures với nhiều rounds
        List<Match> fixtures = createSampleFixturesWithMultipleRounds();
        matchService.setFixturesToReturn(fixtures);
        String startDateStr = "2025-01-01";
        Boolean isRecreate = false;

        // When
        controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: fixturesByRound nên được group đúng
        @SuppressWarnings("unchecked")
        Map<Integer, List<Match>> fixturesByRound = (Map<Integer, List<Match>>) model.getAttribute("fixturesByRound");
        assertNotNull(fixturesByRound, "fixturesByRound không nên null");
        assertTrue(fixturesByRound instanceof TreeMap, "fixturesByRound nên là TreeMap để tự động sort");
        
        // Kiểm tra có đúng số rounds
        long distinctRounds = fixtures.stream()
                .map(m -> Integer.parseInt(m.getMatchDescription().replaceAll("[^0-9]", "")))
                .distinct()
                .count();
        assertEquals(distinctRounds, fixturesByRound.size(), "Số rounds trong fixturesByRound nên đúng");
    }

    // ========== TEST CASE 5: Lưu fixtures cho một round ==========
    @Test
    @DisplayName("TC5: Lưu fixtures cho một round cụ thể")
    void testPostAddFixtures_SaveRound_Success() {
        // Given: Có fixtures trong session, roundNumber = 1
        List<Match> fixtures = createSampleFixturesWithMultipleRounds();
        session.setAttribute("autoFixturesMatch", fixtures);
        // Set empty list for existing matches (no duplicates)
        matchService.setAllMatchesToReturn(new ArrayList<>());
        Integer roundNumber = 1;

        // When
        String result = controller.postAddFixtures(roundNumber, session);

        // Then: Controller trả về redirect với success message
        assertTrue(result.startsWith("redirect:/admin"), "Nên redirect về /admin");
        assertNotNull(matchService.getSavedMatches(), "Nên gọi saveAuto");
        
        // Kiểm tra chỉ lưu matches của round 1
        List<Match> savedMatches = matchService.getSavedMatches();
        assertFalse(savedMatches.isEmpty(), "Nên có matches được lưu");
        savedMatches.forEach(m -> {
            assertTrue(m.getMatchDescription().equals("Vòng 1"), 
                "Chỉ nên lưu matches của Vòng 1");
        });
    }

    // ========== TEST CASE 6: Không lưu khi không có session ==========
    @Test
    @DisplayName("TC6: Không lưu khi session không có fixtures")
    void testPostAddFixtures_NoSession_ReturnsRedirect() {
        // Given: Session không có fixtures
        Integer roundNumber = 1;

        // When
        String result = controller.postAddFixtures(roundNumber, session);

        // Then
        assertEquals("redirect:/admin", result);
        assertNull(matchService.getSavedMatches(), "Không nên gọi saveAuto khi không có session");
    }

    // ========== TEST CASE 7: Lưu round cuối cùng ==========
    @Test
    @DisplayName("TC7: Lưu fixtures cho round cuối cùng")
    void testPostAddFixtures_SaveLastRound_Success() {
        // Given: Fixtures với 3 rounds, lưu round 3
        List<Match> fixtures = createSampleFixturesWithMultipleRounds();
        session.setAttribute("autoFixturesMatch", fixtures);
        // Set empty list for existing matches (no duplicates)
        matchService.setAllMatchesToReturn(new ArrayList<>());
        Integer roundNumber = 3;

        // When
        String result = controller.postAddFixtures(roundNumber, session);

        // Then: Controller trả về redirect với success message
        assertTrue(result.startsWith("redirect:/admin"), "Nên redirect về /admin");
        List<Match> savedMatches = matchService.getSavedMatches();
        assertNotNull(savedMatches, "Nên có matches được lưu");
        savedMatches.forEach(m -> {
            assertTrue(m.getMatchDescription().equals("Vòng 3"), 
                "Chỉ nên lưu matches của Vòng 3");
        });
    }

    // ========== TEST CASE 8: Kiểm tra parse startDate ==========
    @Test
    @DisplayName("TC8: Parse startDate đúng format")
    void testGetAddFixtures_ParseStartDate_Success() {
        // Given: startDate hợp lệ
        String startDateStr = "2025-06-15";
        Boolean isRecreate = false;
        List<Match> fixtures = createSampleFixtures();
        matchService.setFixturesToReturn(fixtures);

        // When
        String result = controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: Không nên có exception
        assertEquals("redirect:/admin", result);
        assertNotNull(session.getAttribute("autoFixturesMatch"), "Nên tạo fixtures thành công");
    }

    // ========== TEST CASE 9: Kiểm tra với empty fixtures ==========
    @Test
    @DisplayName("TC9: Xử lý khi không có fixtures được tạo")
    void testGetAddFixtures_EmptyFixtures_HandlesGracefully() {
        // Given: matchService trả về empty list
        matchService.setFixturesToReturn(new ArrayList<>());
        String startDateStr = "2025-01-01";
        Boolean isRecreate = false;

        // When
        String result = controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: Vẫn redirect nhưng fixtures rỗng
        assertEquals("redirect:/admin", result);
        @SuppressWarnings("unchecked")
        List<Match> sessionFixtures = (List<Match>) session.getAttribute("autoFixturesMatch");
        assertTrue(sessionFixtures.isEmpty(), "Fixtures trong session nên rỗng");
    }

    // ========== TEST CASE 10: Kiểm tra fixturesByRound sort theo key ==========
    @Test
    @DisplayName("TC10: fixturesByRound được sort theo round number")
    void testGetAddFixtures_FixturesByRoundSorted() {
        // Given: Fixtures với rounds không theo thứ tự
        List<Match> fixtures = new ArrayList<>();
        fixtures.add(createMatch("Vòng 3", "Club A vs Club B"));
        fixtures.add(createMatch("Vòng 1", "Club C vs Club D"));
        fixtures.add(createMatch("Vòng 2", "Club E vs Club F"));
        
        matchService.setFixturesToReturn(fixtures);
        String startDateStr = "2025-01-01";
        Boolean isRecreate = false;

        // When
        controller.getAddFixtures(model, startDateStr, session, isRecreate);

        // Then: fixturesByRound nên được sort theo round number
        @SuppressWarnings("unchecked")
        Map<Integer, List<Match>> fixturesByRound = (Map<Integer, List<Match>>) model.getAttribute("fixturesByRound");
        assertNotNull(fixturesByRound);
        
        List<Integer> roundNumbers = new ArrayList<>(fixturesByRound.keySet());
        assertEquals(1, roundNumbers.get(0), "Round đầu tiên nên là 1");
        assertEquals(2, roundNumbers.get(1), "Round thứ hai nên là 2");
        assertEquals(3, roundNumbers.get(2), "Round thứ ba nên là 3");
    }

    // ========== HELPER METHODS ==========
    private List<Club> createDefaultClubs() {
        List<Club> clubs = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Club club = new Club();
            club.setClubId(UUID.randomUUID());
            club.setClubName("Club " + (char)('A' + i - 1));
            club.setClubLogoPath("/images/logoclub/club" + i + ".png");
            club.setIsActive(true);
            clubs.add(club);
        }
        return clubs;
    }

    private List<Match> createSampleFixtures() {
        List<Match> fixtures = new ArrayList<>();
        fixtures.add(createMatch("Vòng 1", "Club A vs Club B"));
        fixtures.add(createMatch("Vòng 1", "Club C vs Club D"));
        return fixtures;
    }

    private List<Match> createSampleFixturesWithMultipleRounds() {
        List<Match> fixtures = new ArrayList<>();
        fixtures.add(createMatch("Vòng 1", "Club A vs Club B"));
        fixtures.add(createMatch("Vòng 1", "Club C vs Club D"));
        fixtures.add(createMatch("Vòng 2", "Club A vs Club C"));
        fixtures.add(createMatch("Vòng 2", "Club B vs Club D"));
        fixtures.add(createMatch("Vòng 3", "Club A vs Club D"));
        fixtures.add(createMatch("Vòng 3", "Club B vs Club C"));
        return fixtures;
    }

    private Match createMatch(String description, String title) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID());
        match.setMatchDescription(description);
        match.setMatchTitle(title);
        match.setMatchStartTime(LocalDateTime.now().plusDays(1));
        
        Club club1 = new Club();
        club1.setClubId(UUID.randomUUID());
        club1.setClubName("Club A");
        
        Club club2 = new Club();
        club2.setClubId(UUID.randomUUID());
        club2.setClubName("Club B");
        
        MatchClubStat stat1 = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, club1);
        MatchClubStat stat2 = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, club2);
        match.setMatchClubStats(List.of(stat1, stat2));
        
        return match;
    }

    // Simple mock HttpSession implementation
    private static class MockHttpSession implements HttpSession {
        private final Map<String, Object> attributes = new java.util.HashMap<>();

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        // Implement other required methods with default behavior
        @Override
        public java.util.Enumeration<String> getAttributeNames() {
            return java.util.Collections.enumeration(attributes.keySet());
        }

        @Override
        public String getId() {
            return "mock-session-id";
        }

        @Override
        public long getCreationTime() {
            return System.currentTimeMillis();
        }

        @Override
        public long getLastAccessedTime() {
            return System.currentTimeMillis();
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
        }

        @Override
        public int getMaxInactiveInterval() {
            return 1800;
        }

        @Override
        public jakarta.servlet.ServletContext getServletContext() {
            return null;
        }

        @Override
        public void invalidate() {
            attributes.clear();
        }

        @Override
        public boolean isNew() {
            return false;
        }
    }
}

