  (function() {
    _showFileContent = function(data) {
      ret = JSON.parse(data);
      $("#text").show();
      text = ret.documentRope.sequence;
      $("#text textarea").val(text);
    };

    showFileContent = function() {

      fileName = $(this).text();
      $.ajax({
        url: "/documents/d?documentId="+fileName,
        type: "GET",
        success: _showFileContent,
      });
    };

    updateFile = function(name) {
      ul = $("#showFiles");
      ul.append('<li><a href="#">' + name + '</a></li>');
      $("#allFiles li").click(showFileContent);

    };

    addFile = function() {
      var name;
      name = $("form#addfile input#filename").val();
      if(name === "") {
        return false;
      }
      $("form#addfile input#username").val("");
      $.ajax({
        url: "/documents?name="+name,
        type: "POST",
        data: name,
        success: updateFile(name),
      });
      return false;
    };

    _showFiles = function(data) {
      $("#adduser").hide();
      $("#allUsers").hide();

      $("#userList h2").html("Files");

      files = $("#allFiles");
      files.show();
      files.empty();

      $("#addfile").show();
      ul = $("<ul id=showFiles/>");
      var name, ret, ul, users, _i, _len, _ref;
      ret = JSON.parse(data);
      console.log(ret);
      if(ret == null || ret.length == 0) {
        files.append("No file.");
        return;
      }

      _ref = ret;
      for(_i=0, _len=_ref.length; _i < _len; _i++) {
        console.log(_ref[_i]);

        name = _ref[_i];
        ul.append('<li><a href="#">' + name + '</a></li>');
      }
      files.append(ul);
      $("#allFiles li").click(showFileContent);
    };

    showUserFiles = function() {
      $.ajax({
        url: "/documents",
        type: "GET",
        success: _showFiles,
        cache: false
      });
    };

    updateUser = function(name) {
      ul = $("#updateUsers");
      ul.append('<li><a href="#">' + name + '</a></li>');
      $("#allUsers li").click(showUserFiles);
    }
    updateUsers = function(data) {
      var name, ret, ul, users, _i, _len, _ref;
      ret = JSON.parse(data);
      users = $("#allUsers");

      if(ret == null) {
        users.append("No user.");
        return;
      }

      ul = $("<ul id=updateUsers/>");
      _ref = ret;

      for(_i=0, _len=_ref.length; _i < _len; _i++) {
        name = _ref[_i];
        ul.append('<li><a href="#">' + name + '</a></li>');
      }
      users.append(ul);
      $("#allUsers li").click(showUserFiles);
    };

    listUsers = function() {
      $.ajax({
        url: "/users",
        type: "GET",
        success: updateUsers,
        cache: false
      });
    };

    addUser = function() {
      var name;
      name = $("form#adduser input#username").val();
      if(name === "") {
        returnfalse;
      }
      $("form#adduser input#username").val("");
      $.ajax({
        url: "/users?user="+name,
        type: "POST",
        data: name,
        success:updateUser(name)
      });
      return false;
    };

testShowFile = function(data) {
  fileName = $(this).text();
  data = "test data";
  $("#text").show();
  $("#text textarea").val(data);
};

testShowUserFiles = function() {
  $("#adduser").hide();
  userName = $(this).text();
  users = $("#allUsers");
  users.empty();
  ul = $("<ul/>");
  ul.append('<li><a href="#">' + userName + "File" + '</a></li>');
  users.append(ul);
  $("#allUsers li").click(testShowFile);
};

    main = function() {
      $("form#adduser").submit(addUser);
      $("form#addfile").submit(addFile);
    };

    $(document).ready(main);
    $(document).ready(listUsers);

  }).call(this);
