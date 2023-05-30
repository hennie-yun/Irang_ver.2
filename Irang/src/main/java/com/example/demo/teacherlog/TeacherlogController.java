package com.example.demo.teacherlog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.child.Child;
import com.example.demo.child.ChildDto;
import com.example.demo.child.ChildService;
import com.example.demo.tcomment.TcommentDto;
import com.example.demo.tcomment.TcommentService;
import com.example.demo.teacher.Teacher;

@Controller
@RequestMapping("/teacherlog")
public class TeacherlogController {
	@Autowired
	private TeacherlogService service;

	@Autowired
	private TcommentService cService;

	@Autowired
	private ChildService childService;

	@Value("${spring.servlet.multipart.location}")
	private String path;

	@GetMapping("/add")
	public String addForm(ModelMap map) {
		map.addAttribute("bodyview", "/WEB-INF/views/teacherlog/add.jsp");
		return "index";
	}

	@PostMapping("/add")
	public String add(ModelMap map, TeacherlogDto dto) {
		int num = service.save(dto);
		File dir = new File(path + num);
		dir.mkdir();

		MultipartFile[] f = dto.getF();
		String[] imgs = new String[3];

		for (int i = 0; i < f.length; i++) {
			MultipartFile x = f[i];
			String fname = x.getOriginalFilename();// 원본파일명
			if (fname != null && !fname.equals("")) {
				// String newpath = path + num + "/" + fname;
				File newfile = new File(path + num + "/" + fname);
				// System.out.println(newpath);
				try {
					x.transferTo(newfile);// 파일 업로드
					imgs[i] = fname;
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		dto.setImg1(imgs[0]);
		dto.setImg2(imgs[1]);
		dto.setImg3(imgs[2]);
		dto.setTlnum(num);
		service.save(dto);// 수정
		// 어디로 가지? 목록?

		return "redirect:/teacherlog/list?teacherid=" + dto.getTeacherid().getTeacherid();
	}

	// 특정쌤이 쓴 일지리스트
	@GetMapping("/list")
	public String tlist(Teacher teacherid, ModelMap map) {
		map.addAttribute("list", service.getByTeacherId(teacherid));
		System.out.println(service.getByTeacherId(teacherid));
		map.addAttribute("bodyview", "/WEB-INF/views/teacherlog/t-list.jsp");
		return "index";
	}

	// 특정쌤이 본 디테일 페이지 여기서 수정 가능하죠?
	@GetMapping("/detail")
	public String tDetail(Teacherlog tlnum, ModelMap map) {
		map.addAttribute("vo", service.getByTlnum(tlnum.getTlnum()));
		ArrayList<TcommentDto> list = cService.getByTlnum(tlnum);
		map.addAttribute("list", list);
		map.addAttribute("bodyview", "/WEB-INF/views/teacherlog/t-detail.jsp");
		return "index";
	}

	// 특정보호자 아이디로 본 일지 리스트
	@GetMapping("/childList")
	public String tChildList(Child childid, ModelMap map) {
		map.addAttribute("list", service.getByChildId(childid));
		map.addAttribute("bodyview", "/WEB-INF/views/teacherlog/t-list.jsp");
		return "index";
	}

	// 특정보호자가 본 디테일 페이지
	@GetMapping("/childDetail")
	public String tChildDetail(int tlnum, ModelMap map) {
		map.addAttribute("vo", service.getByTlnum(tlnum));
		map.addAttribute("bodyview", "/WEB-INF/views/teacherlog/t-detail.jsp");
		return "index";
	}

	// 디테일 : 사진 불러오기
	@GetMapping("/read_img")
	public ResponseEntity<byte[]> read_img(String fname, int tlnum) {
		File f = new File(path + tlnum + '/' + fname);
		System.out.println(path + tlnum + '/' + fname);
		HttpHeaders header = new HttpHeaders(); // HttpHeaders 객체 생성
		ResponseEntity<byte[]> result = null; // 선언
		try {
			header.add("Content-Type", Files.probeContentType(f.toPath()));// 응답 데이터의 종류를 설정
			result = new ResponseEntity<byte[]>(FileCopyUtils.copyToByteArray(f), header, HttpStatus.OK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	// 디테일 : 수정하기
	@PostMapping("/edit")
	@ResponseBody
	public Map edit(TeacherlogDto dto) {
		Map map = new HashMap();
		System.out.println("dto :" + dto);
		TeacherlogDto dto2 = service.getByTlnum(dto.getTlnum());
		dto2.setTlnum(dto.getTlnum());
		dto2.setActivity(dto.getActivity());
		dto2.setHealth(dto.getHealth());
		System.out.println("dto2 :" + dto2);
		service.save(dto2);
		map.put("vo", dto2);
		return map;
	}

	// 디테일 : 삭제하기
	@GetMapping("/del")
	public String del(int tlnum, String teacherid) {
		service.delete(tlnum);

		String delPath = path + tlnum;
		File dir = new File(delPath);
		File[] files = dir.listFiles(); // 디렉토리 안에 있는 파일들을 File 객체로 생성해서 반환
		for (File f : files) { // 상품 이미지를 삭제
			f.delete();
		}
		dir.delete(); // 디렉토리 삭제
		service.delete(tlnum); // db에서 행삭제
		return "redirect:/teacherlog/list?teacherid=" + teacherid;
	}

	// 특정 쌤이 쓴 리스트 : 날짜로 검색
	@GetMapping("/day")
	@ResponseBody
	public Map day(LocalDate tdate, Teacher teacherid) {
		Map map = new HashMap();
		ArrayList<TeacherlogDto> list = service.getByDayAndTeacherid(tdate, teacherid);
		map.put("list", list);
		return map;
	}

	// 보호자 입장 전체 리스트 : 날짜로 검색
	@GetMapping("/childDay")
	@ResponseBody
	public Map childDay(LocalDate tdate, Child childid) {
		Map map = new HashMap();
		ArrayList<TeacherlogDto> list = service.getByDayAndChildid(tdate, childid);
		map.put("list", list);
		return map;
	}

	// 특정 쌤이 쓴 리스트 : 아이 이름으로 검색
	@GetMapping("/searchName")
	@ResponseBody
	public Map searchName(String name, String teacherid) {
		Map map = new HashMap();
		ArrayList<TeacherlogDto> list = new ArrayList<TeacherlogDto>();

		ArrayList<ChildDto> clist = childService.getByName(name);
		System.out.println("clist :"+clist);
		for (ChildDto vo : clist) {
			Child child = convertToChild(vo);
			ArrayList<TeacherlogDto> list2 = service.getByChildId(child);
			for (TeacherlogDto dto : list2) {
				System.out.println("dto.getTeacherid() :"+dto.getTeacherid());
				System.out.println("teacherid :"+teacherid);
				if(dto.getTeacherid().getTeacherid().equals(teacherid)) {
					list.add(dto);
				}
			}
		}

		System.out.println("list :" + list);
		map.put("list", list);
		return map;
	}

	// 난 teacherlog에 child라고 써놨고.. 하지만 childservice는 childdto를 반환하기 때문에...
	// ChildDto를 Child로 변경해주기 위해서...
	private Child convertToChild(ChildDto childDto) {
		Child child = new Child();
		child.setChildid(childDto.getChildid());
		return child;
	}

}
