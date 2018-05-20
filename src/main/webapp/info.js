  (function() {
    /*
  _showUserFiles = function(data) {
    var text;
    ret = JSON.parse(data);
    text = ret.Text;
    if(ret.Err != "") {
      return;
    }
    $("#text textarea").val(text);
  };

  showFile = function() {
    fileName = $(this).text();
    $.ajax({
      url: "documents/",
      type: "GET",
      data: filename,
      success: _showFile,
      cache: false
    })
  };
*/
    addFile = function() {
      var name;
      name = $("form#addfile input#filename").val();
      if(name === "") {
        return false;
      }
      //$("form#adduser input#username").val("");
      $.ajax({
        url: "/documents?name="+name,
        type: "POST",
        data: name,
      });
    };

    _showFiles = function(data) {
      $("#adduser").hide();
      $("#allUsers").hide();

      $("#userList h2").html("Files");

      files = $("#allFiles");
      files.show();
      files.empty();

      $("#addfile").show();

      var name, ret, ul, users, _i, _len, _ref;
      ret = JSON.parse(data);
      console.log(ret);
      if(ret == null || ret.length == 0) {
        files.append("No file.");
        return;
      }
      ul = $("<ul/>");

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
      })
    };

    updateUsers = function(data) {
      var name, ret, ul, users, _i, _len, _ref;
      ret = JSON.parse(data);
      users = $("#allUsers");

      if(ret == null) {
        users.append("No user.");
        return;
      }

      ul = $("<ul/>");
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
    }();

    addUser = function() {
      var name;
      name = $("form#adduser input#username").val();
      if(name === "") {
        return false;
      }
      //$("form#adduser input#username").val("");
      $.ajax({
        url: "/users?user="+name,
        type: "POST",
        data: name,
      });
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
