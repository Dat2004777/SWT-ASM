<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>${empty staff ? 'Add Staff' : 'Edit Staff'}</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container">
    <h2 class="text-center mt-5">${empty staff ? 'Add New Staff' : 'Edit Staff'}</h2>

    <!-- Error Message Display -->
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger text-center" role="alert">
            ${errorMessage}
        </div>
    </c:if>

    <form action="staff-crud" method="post">
        <input type="hidden" name="action" value="${empty staff ? 'create' : 'update'}">
        <c:if test="${not empty staff}">
            <input type="hidden" name="staffID" value="${staff.staffID}">
        </c:if>
        <div class="form-group">
            <label for="staffCode">Staff Code</label>
            <input type="text" class="form-control" id="staffCode" name="staffCode"
                   value="<c:out value='${staff.staffCode}'/>" required maxlength="20"
                   pattern="[A-Za-z0-9]{3,20}"
                   title="3-20 letters or digits.">
        </div>
        <div class="form-group">
            <label for="fullName">Full Name</label>
            <input type="text" class="form-control" id="fullName" name="fullName"
                   value="<c:out value='${staff.fullName}'/>" required maxlength="100">
        </div>
        <div class="form-group">
            <label for="dateOfBirth">Date of Birth</label>
            <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth"
                   value="<c:out value='${staff.dateOfBirth}'/>" required>
        </div>
        <div class="form-group">
            <label>Gender</label><br>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="gender" id="male" value="true" ${staff.gender ? 'checked' : ''} required>
                <label class="form-check-label" for="male">Male</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="gender" id="female" value="false" ${!staff.gender ? 'checked' : ''}>
                <label class="form-check-label" for="female">Female</label>
            </div>
        </div>
        <div class="form-group">
            <label for="phoneNumber">Phone Number</label>
            <input type="text" class="form-control" id="phoneNumber" name="phoneNumber" value="${staff.phoneNumber}"
                   required
                   maxlength="10"
                   pattern="0[0-9]{9}"
                   title="Phone number must be 10 digits and start with 0.">
        </div>
        <div class="form-group">
            <label for="email">Email</label>
            <input type="email" class="form-control" id="email" name="email"
                   value="<c:out value='${staff.email}'/>" required maxlength="100">
        </div>
        <div class="form-group">
            <label for="department">Department</label>
            <input type="text" class="form-control" id="department" name="department"
                   value="<c:out value='${staff.department}'/>" required maxlength="100">
        </div>
        <div class="form-group">
            <label for="position">Position</label>
            <input type="text" class="form-control" id="position" name="position"
                   value="<c:out value='${staff.position}'/>" required maxlength="100">
        </div>
        <div class="form-group">
            <label for="salary">Salary (VND)</label>
            <input type="number" class="form-control" id="salary" name="salary"
                   value="<c:out value='${staff.salary}'/>" required min="1000000" max="1000000000" step="1000">
        </div>
        <div class="form-group">
            <label for="hireDate">Hire Date</label>
            <input type="date" class="form-control" id="hireDate" name="hireDate"
                   value="<c:out value='${staff.hireDate}'/>" required>
        </div>
        <c:if test="${empty staff || staff.staffID == 0}">
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" class="form-control" id="password" name="password" required maxlength="50">
            </div>
        </c:if>
        <div class="form-group">
            <label for="roleID">Role</label>
            <select class="form-control" id="roleID" name="roleID" required>
                <c:forEach var="role" items="${roleList}">
                    <option value="${role.roleID}" ${staff.role.roleID == role.roleID ? 'selected' : ''}>
                        ${role.roleName}
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="form-group">
            <label>Status</label><br>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="isActive" id="active" value="true" ${staff.isActive ? 'checked' : ''} required>
                <label class="form-check-label" for="active">Active</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="isActive" id="inactive" value="false" ${!staff.isActive ? 'checked' : ''}>
                <label class="form-check-label" for="inactive">Inactive</label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">${empty staff ? 'Create' : 'Update'}</button>
        <a href="staff-list" class="btn btn-secondary">Cancel</a>
    </form>
</div>
</body>
</html>
