package com.swp.myleague.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.admin_request.Request;
import com.swp.myleague.model.entities.admin_request.RequestStatus;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.blog.BlogCategory;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.information.PlayerPosition;
import com.swp.myleague.model.repo.BlogRepo;
import com.swp.myleague.model.service.RequestService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchEventService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.entities.match.Match;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = { "/clubmanager", "/clubmanage/" })
public class ClubManagementController {

    @Autowired
    ClubService clubService;

    @Autowired
    PlayerService playerService;

    @Autowired
    BlogService blogService;

    @Autowired
    UserService userService;

    @Autowired
    RequestService requestService;

    @Autowired
    BlogRepo blogRepo;

    @Autowired
    MatchEventService matchEventService;

    @Autowired
    MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("")
    public String getClub(Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        
        // Lấy tất cả matches của club (cả quá khứ và tương lai)
        List<Match> allMatches = matchService.getAll().stream()
                .filter(m -> m.getMatchClubStats().stream()
                        .anyMatch(mcs -> mcs.getClub().getClubId().equals(club.getClubId())))
                .sorted((m1, m2) -> m2.getMatchStartTime().compareTo(m1.getMatchStartTime())) // Sắp xếp mới nhất trước
                .collect(Collectors.toList());
        
        model.addAttribute("club", club);
        model.addAttribute("positions", PlayerPosition.values());
        model.addAttribute("categories", BlogCategory.values());
        model.addAttribute("matches", allMatches);
        return "ClubManagementPage";
    }

    @PostMapping("/updateinfo")
    public String updateClubInfo(@RequestParam(name = "clubName") String clubName,
            @RequestParam(name = "clubStadium") String clubStadium,
            @RequestParam(name = "clubDescription") String clubDescription,
            @RequestParam(name = "clubPrimaryColor") String clubPrimaryColor,
            @RequestParam(name = "clubSecondaryColor") String clubSecondaryColor,
            @RequestParam(name = "clubId") String clubId,
            Model model) {
        Club club = clubService.getById(clubId);
        club.setClubName(clubName);
        club.setClubStadium(clubStadium);
        club.setClubDescription(clubDescription);
        club.setClubPrimaryColor(clubPrimaryColor);
        club.setClubSecondaryColor(clubSecondaryColor);
        club = clubService.save(club);
        return "redirect:/clubmanager";
    }

    @PostMapping("/addplayer")
    public String addPlayer(
            @RequestParam(name = "playerFullName") String playerName,
            @RequestParam(name = "position") String playerPosition,
            @RequestParam(name = "playerNationaly") String playerNationaly,
            @RequestParam(name = "playerNumber") String playerNumber,
            @RequestParam("playerImage") MultipartFile playerImage, // mới thêm nếu có upload ảnh
            @RequestParam("playerDob") String dob,
            @RequestParam("playerHeight") String height,
            @RequestParam("playerWeight") String weight,
            Principal principal, RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());

        List<Player> getPlayers = playerService.getPlayersByClubId(club.getClubId().toString());

        if (getPlayers.stream().anyMatch(p -> p.getPlayerFullName().equals(playerName)
                || p.getPlayerNumber() == Integer.parseInt(playerNumber))) {
            redirectAttributes.addFlashAttribute("message", "The player is existed");
            return "redirect:/clubmanager";
        }

        Player player = new Player();
        player.setClub(club);
        player.setPlayerFullName(playerName);
        player.setPlayerPosition(Arrays.stream(PlayerPosition.values())
                .filter(pp -> pp.name().equalsIgnoreCase(playerPosition))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid player position: " + playerPosition)));
        player.setPlayerNationaly(playerNationaly);
        player.setPlayerNumber(Integer.parseInt(playerNumber));
        player.setPlayerInformation("dob: " + dob + "\nheight: " + height + "\nweight: " + weight);
        player.setPlayerAppearances(0);
        player.setPlayerAssist(0);
        player.setPlayerCleanSheets(0);
        player.setPlayerScores(0);

        // Xử lý ảnh cầu thủ (nếu có gửi lên)
        if (!playerImage.isEmpty()) {
            File imageFile = new File(
                    "src/main/resources/static/images/Storage-Files" + File.separator
                            + playerImage.getOriginalFilename());
            try {
                Files.copy(playerImage.getInputStream(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                player.setPlayerImgPath("/images/Storage-Files/" + playerImage.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace(); // có thể log ra logger
            }
        }

        // Tạo request để xác nhận thêm cầu thủ
        Request request = new Request();
        request.setRequestTitle("CREATE_PLAYER_FROM_" + club.getClubName());
        request.setRequestInfor(player.toString());
        request.setRequestStatus(RequestStatus.PENDING);
        requestService.save(request);

        return "redirect:/clubmanager";
    }

    @PostMapping("/editplayer")
    public String editPlayer(@RequestParam(name = "playerFullName") String playerName,
            @RequestParam(name = "position") String playerPosition,
            @RequestParam(name = "playerNationaly") String playerNationaly,
            @RequestParam(name = "playerNumber") String playerNumber,
            @RequestParam(name = "playerId") String playerId,
            Model model, Principal principal) {
        Player player = playerService.getById(playerId);
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        player.setClub(club);
        player.setPlayerFullName(playerName);
        player.setPlayerPosition(Arrays.asList(PlayerPosition.values()).stream()
                .filter(pp -> pp.name().equals(playerPosition)).findFirst().orElseThrow());
        player.setPlayerNationaly(playerNationaly);
        player.setPlayerNumber(Integer.parseInt(playerNumber));

        player = playerService.save(player);
        return "redirect:/clubmanager";
    }

    @PostMapping("/addblog")
    public String addBlog(@RequestParam(name = "title") String blogTitle,
            @RequestParam(name = "content") String blogContent,
            @RequestParam("thumnailFile") MultipartFile thumnailFile,
            Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        Blog blog = new Blog();
        File newFile = new File(
                "src/main/resources/static/images/Storage-Files" + File.separator + thumnailFile.getOriginalFilename());
        try {

            Files.copy(thumnailFile.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            blog.setBlogThumnailPath("/images/Storage-Files/" + thumnailFile.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        blog.setBlogTitle(blogTitle);
        String htmlContent = blogContent.replace("\n", "<br/>");
        blog.setBlogContent(htmlContent);
        blog.setClub(club);
        blog.setBlogDateCreated(LocalDateTime.now());
        blog.setBlogCategory(BlogCategory.Hotnews);
        Request request = new Request();
        request.setRequestTitle("CREATE_BLOG_FROM_" + club.getClubName());
        try {
            String json = objectMapper.writeValueAsString(blog);
            request.setRequestInfor(json);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        request.setRequestStatus(RequestStatus.PENDING);
        requestService.save(request);

        return "redirect:/clubmanager";
    }

    @PostMapping("/editblog")
    public String editBlog(@RequestParam(name = "title") String blogTitle,
            @RequestParam(name = "content") String blogContent,
            @RequestParam(name = "blogId") String blogId,
            Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());

        Blog blog = blogService.getById(blogId);
        blog.setBlogTitle(blogTitle);
        blog.setBlogContent(blogContent);
        blog.setClub(club);
        blog.setBlogDateCreated(LocalDateTime.now());

        blog = blogService.save(blog);

        return "redirect:/clubmanager";
    }

    @PostMapping("/exportplayers")
public void exportPlayers(HttpServletResponse response, Principal principal) throws IOException {
    String username = principal.getName();
    User user = userService.findByUsername(username);
    Club club = clubService.getByUserId(user.getUserId());

    // Làm sạch tên club để tránh ký tự đặc biệt gây lỗi file name
    String safeClubName = club.getClubName().replaceAll("[^a-zA-Z0-9_\\-]", "_");

    // Cấu hình response header để tải file
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition",
            "attachment; filename=" + safeClubName + "_players.xlsx");

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Players");

    // Header
    Row headerRow = sheet.createRow(0);
    String[] headers = { "Player id", "Full Name", "Position", "Number", "Nationality", "Lineup Type" };
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
    }

    // Dữ liệu
    int rowIdx = 1;
    for (Player player : club.getPlayers()) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(player.getPlayerId().toString());
        row.createCell(1).setCellValue(player.getPlayerFullName());
        row.createCell(2).setCellValue(player.getPlayerPosition().name());
        row.createCell(3).setCellValue(player.getPlayerNumber());
        row.createCell(4).setCellValue(player.getPlayerNationaly());
        row.createCell(5).setCellValue(""); // cột Lineup Type để trống, user chọn từ dropdown
    }

    // ==== Tạo dropdown list cho cột "Lineup Type" ====
    String[] lineupOptions = { "Sub", "Starter" };
    DataValidationHelper validationHelper = sheet.getDataValidationHelper();
    DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(lineupOptions);

    int firstRow = 1;
    int lastRow = rowIdx - 1;
    int column = 5;

    CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, column, column);
    DataValidation validation = validationHelper.createValidation(constraint, addressList);
    validation.setSuppressDropDownArrow(true);
    validation.setShowErrorBox(true);
    sheet.addValidationData(validation);

    // Tự động căn chỉnh độ rộng các cột
    for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
    }

    workbook.write(response.getOutputStream());
    workbook.close();
}


}