<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>Staff List</title>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
        </head>

        <body>
            <div class="container-fluid px-5">
                <h2 class="text-center mt-4">Staff Management</h2>
                <form action="staff-list" method="get" class="form-row align-items-end mb-3">
                    <div class="col"><label>Name</label><input class="form-control" name="searchName"
                            value="<c:out value='${param.searchName}'/>" placeholder="Search by name"></div>
                    <div class="col"><label>Staff ID</label><input class="form-control" type="number" min="1"
                            name="staffId" value="<c:out value='${param.staffId}'/>"></div>
                    <div class="col"><label>Status</label><select class="form-control" name="searchStatus">
                            <option value="">All statuses</option>
                            <option value="true" ${param.searchStatus=='true' ?'selected':''}>Active</option>
                            <option value="false" ${param.searchStatus=='false' ?'selected':''}>Inactive</option>
                        </select></div>
                    <div class="col-auto"><button class="btn btn-primary">Search</button> <a
                            class="btn btn-outline-secondary" href="staff-list">Reset</a></div>
                </form>
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/staff-crud?action=create" class="btn btn-success">Add Staff</a>
                     <a href="${pageContext.request.contextPath}/logout"
                        class="btn btn-danger">
                        Logout
                    </a>
                    <span class="float-right">${pageResult.totalItems} result(s)</span>
                </div>
                <table class="table table-bordered table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Full Name</th>
                            <th>Gender</th>
                            <th>Phone</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty staffList}">
                                <tr>
                                    <td colspan="8" class="text-center text-muted">No staff found.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="staff" items="${staffList}">
                                    <tr>
                                        <td>${staff.staffID}</td>
                                        <td>
                                            <c:out value="${staff.fullName}" />
                                        </td>
                                        <td>${staff.gender?'Male':'Female'}</td>
                                        <td>
                                            <c:out value="${staff.phoneNumber}" />
                                        </td>
                                        <td>
                                            <c:out value="${staff.email}" />
                                        </td>
                                        <td>
                                            <c:out value="${staff.role.roleName}" />
                                        </td>
                                        <td>${staff.isActive?'Active':'Inactive'}</td>
                                        <td><a href="staff-crud?action=edit&id=${staff.staffID}"
                                                class="btn btn-sm btn-warning">Edit</a> <a
                                                href="staff-crud?action=delete&id=${staff.staffID}"
                                                class="btn btn-sm btn-danger"
                                                onclick="return confirm('Are you sure?')">Delete</a></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
                <c:url var="pageUrl" value="staff-list">
                    <c:param name="searchName" value="${param.searchName}" />
                    <c:param name="staffId" value="${param.staffId}" />
                    <c:param name="searchStatus" value="${param.searchStatus}" />
                </c:url>
                <nav>
                    <ul class="pagination justify-content-center">
                        <li class="page-item ${pageResult.hasPrevious?'':'disabled'}"><a class="page-link"
                                href="${pageUrl}&page=${pageResult.currentPage-1}">Previous</a></li>
                        <c:forEach begin="1" end="${pageResult.totalPages}" var="p">
                            <li class="page-item ${p==pageResult.currentPage?'active':''}"><a class="page-link"
                                    href="${pageUrl}&page=${p}">${p}</a></li>
                        </c:forEach>
                        <li class="page-item ${pageResult.hasNext?'':'disabled'}"><a class="page-link"
                                href="${pageUrl}&page=${pageResult.currentPage+1}">Next</a></li>
                    </ul>
                </nav>
            </div>
        </body>

        </html>