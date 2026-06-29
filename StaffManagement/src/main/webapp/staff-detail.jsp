<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Staff Detail</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container">
    <h2 class="text-center mt-5">Staff Detail</h2>

    <c:choose>
        <c:when test="${empty staff}">
            <div class="alert alert-warning text-center" role="alert">
                Staff not found.
            </div>
            <div class="text-center">
                <a href="staff-list" class="btn btn-secondary">Back to list</a>
            </div>
        </c:when>
        <c:otherwise>
            <table class="table table-bordered mt-4">
                <tbody>
                    <tr>
                        <th style="width: 30%;">ID</th>
                        <td>${staff.staffID}</td>
                    </tr>
                    <tr>
                        <th>Staff Code</th>
                        <td><c:out value="${staff.staffCode}"/></td>
                    </tr>
                    <tr>
                        <th>Full Name</th>
                        <td><c:out value="${staff.fullName}"/></td>
                    </tr>
                    <tr>
                        <th>Date of Birth</th>
                        <td><c:out value="${staff.dateOfBirth}"/></td>
                    </tr>
                    <tr>
                        <th>Gender</th>
                        <td>${staff.gender ? 'Male' : 'Female'}</td>
                    </tr>
                    <tr>
                        <th>Phone Number</th>
                        <td><c:out value="${staff.phoneNumber}"/></td>
                    </tr>
                    <tr>
                        <th>Email</th>
                        <td><c:out value="${staff.email}"/></td>
                    </tr>
                    <tr>
                        <th>Department</th>
                        <td><c:out value="${staff.department}"/></td>
                    </tr>
                    <tr>
                        <th>Position</th>
                        <td><c:out value="${staff.position}"/></td>
                    </tr>
                    <tr>
                        <th>Salary (VND)</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty staff.salary}">
                                    <fmt:formatNumber value="${staff.salary}" type="number" groupingUsed="true"/>
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Hire Date</th>
                        <td><c:out value="${staff.hireDate}"/></td>
                    </tr>
                    <tr>
                        <th>Role</th>
                        <td><c:out value="${staff.role.roleName}"/></td>
                    </tr>
                    <tr>
                        <th>Status</th>
                        <td>${staff.isActive ? 'Active' : 'Inactive'}</td>
                    </tr>
                </tbody>
            </table>

            <div class="mb-5">
                <a href="staff-list" class="btn btn-secondary">Back to list</a>
                <a href="staff-crud?action=edit&id=${staff.staffID}" class="btn btn-warning">Edit</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
