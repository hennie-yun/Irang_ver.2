<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous">
<link rel="stylesheet" href="/css/teacher.css" type="text/css">
<script>
$(document).ready(function(){
	$('.changeclass').click(function(){
		
	})
})
</script>
</head>
<body>

	<!-- JavaScript Bundle with Popper -->
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
		integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
		crossorigin="anonymous"></script>
		
<!-- 	이름으로 검색 -->
	<form action="/teacher/getbyname" >
	선생님 이름 <input type="text" name="name">
	<input type="submit" value="검색">
	</form>
	
	
	<div class="row body" style="margin:10px;">
	<c:forEach var="li" items="${list }">
		<div class="card" style="width: 18rem;">
			<img src="/teacher/read_img?fname=${li.profile }" class="card-img-top" alt="...">
			<div class="card-body">
				<h5 class="card-title">${li.name }</h5>
				<p class="card-text">
						<select id="inputState" class="form-control changeclass" name="classnum">
		            	<c:forEach var="cl" items = "${classlist }">
		            		<option value="<c:out value="${cl.classnum}"/>"><c:out value="${cl.classname }"/>
		            	</c:forEach>
		            	</select>
					${li.classnum.classname}<br/>					
					
				</p>
			</div>
		</div>
	</c:forEach>
	</div>
</body>
</html>