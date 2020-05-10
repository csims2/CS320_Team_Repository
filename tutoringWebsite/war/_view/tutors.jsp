<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/_view/main.css" >
        <Title>Tutors</Title>
    </head>
    <body>
        <form action="${pageContext.servletContext.contextPath}/index" method="post">
            <div id = "titleDiv">
                <input id = "title" name="index" type="submit" value="Kinsley Tutoring Service" /><br>			
            </div>  
            <div class = "navBar">
                <input id="navBarItem" name="courses" type="submit" value="Courses" />
                <input id="navBarItem" name="groups" type="submit" value="Study Groups" />
                <input id="navBarItem" name="schedule" type="submit" value="Schedule" />
                <input id="navBarItem" name="tutors" type="submit" value="Tutors" />
                <input id="navBarItem" name="resources" type="submit" value="Resources" />
                <input id="navBarItem" name="profile" type="submit" value="Profile" />
                <input id="navBarItem" name="login" type="submit" value="Login" />
                <input id="navBarItem" name="createAccount" type="submit" value="Create Account" />
            </div>
             </form>
               <form action="${pageContext.servletContext.contextPath}/tutors" method="get">
                
            <div class = "pageDesc">
	           <p>Tutors</p>
	           </div>
	              <div class = "profileDis">
	           <table>
                    <tr>
                     	<td>Tutor</td>
                        <td>Email:</td>      	
                   </tr>
                   
                    <c:forEach items="${tutorList}" var="tutor">
			        	<tr class="scheduleListings">
				           	 <td>${tutor.name}</td>		
				            	<td>${tutor.email}</td>          
			        	</tr>
			    	</c:forEach>
			    	 </table>
			    	 </div>
			    	 </form>
			    	 <form action="${pageContext.servletContext.contextPath}/coursePage" method="get">
			    	  <div class = "tutorDis">
			    	 <table>
			    	  <tr>
                     	<td>Courses</td>  	
                   </tr>
                   
			    	 <c:forEach items="${courseList}" var="course">
			        	<tr>	
				           	 <td><input id="courseTitle" name="${course[1].courseId}" type="submit" value="${course[1].title}" class = "myButton"</td> 
				           	<c:if test="${ not empty course[2].title }">
				            	<td><input id="courseTitle" name="${course[2].courseId}" type="submit" value="${course[2].title}" class = "myButton"</td>
				            </c:if> 
				              	<c:if test="${ not empty course[3].title }">
				           	  <td><input id="courseTitle" name="${course[3].courseId}" type="submit" value="${course[3].title}" class = "myButton"</td> 
				           	  </c:if> 
			        	</tr>
			    	</c:forEach>
			    </table>
          </form>
         </div>
        
    </body>

</html>|