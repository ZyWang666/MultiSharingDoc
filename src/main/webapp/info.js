(function() {
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

    _showUserFiles = function(data) {
      $("#adduser").hide();
      var name, ret, ul, users, _i, _len, _ref;
      ret = JSON.parse(data);
      if(ret.Err != "") {
        return;
      }
      users = $("#allUsers");
      users.empty();
      if(ret.Users == null || ret.Users.length == 0) {
        users.append("No file.");
        return;
      }
      ul = $("<ul/>");
      _ref = ret.Users;
      for(_i=0, _len=_ref.length; _i < _len; _i++) {
        name = _ref[_i];
        ul.append('<li><a href="#">' + name + '</a></li>');
      }
      users.append(ul);
      $("#allUsers li").click(showFile);
    };

    showUserFiles = function(name) {
      $.ajax({
        url: "",
        type: "POST",
        data: name,
        success: _showUserFiles,
        cache: false
      })
    }

    updateUsers = function(data) {
    var name, ret, ul, users, _i, _len, _ref;
    ret = JSON.parse(data);
    if(ret.Err != "") {
      return;
    }
    users = $("#allUsers");
    users.empty();
    if(ret.Users == null || ret.Users.length == 0) {
      users.append("No user.");
      return;
    }
    ul = $("<ul/>");
    _ref = ret.Users;
    for(_i=0, _len=_ref.length; _i < _len; _i++) {
      name = _ref[_i];
      ul.append('<li><a href="#">' + name + '</a></li>');
    }
    console.log("update users");
    users.append(ul);
    $("#allUsers li").click(showUserFiles);
    };

  listUsers = function() {
    $.ajax({
      url: "",
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
    $("form#adduser input#username").val("");
    console.log("addddd user", name);
    $.ajax({
      url: "",
      type: "POST",
      data: name,
      success: updateUsers
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

testUsers = function() {
  users = $("#allUsers");
  ul = $("<ul/>");
  ul.append('<li id=test><a href="#">' + "testUserA" + '</a></li>');
  users.append(ul);
  $("#allUsers li").click(testShowUserFiles);
}();

  main = function() {
    $("form#adduser").submit(addUser);
  };

  $(document).ready(main);

}).call(this);
