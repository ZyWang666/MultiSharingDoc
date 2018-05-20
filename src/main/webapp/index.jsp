<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>WYSIWYG</title>
    <link href="./style.css" rel="stylesheet" type="text/css" media="all"/>
    <!-- Add manifest -->
    <link rel="manifest" href="manifest.json"/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
</head>

<body><div id="body">
    <div id="top">
        <div id="userList">
            <h2 id="theTitle">Users</h2>
            <div id="allUsers">
            </div>
            <div id="allFiles">
            </div>
            <div class="adduser">
                <form id="adduser" action="#" method="post">
                    <input id="username" type="input" class="input"/>
                    <input class="button" type="submit" value="Add User"/>
                </form>
            </div>
            <div class="addfile">
                <form id="addfile" action="#" method="post">
                    <input id="filename" type="input" class="input"/>
                    <input class="button" type="submit" value="Add File"/>
                </form>
            </div>
        </div>
    </div>
    <div id="text">
        <div id="compose">
                <textarea class="input" name="post" rows="40" cols="100"></textarea>

    </div>
    </div>
</div>
</body>
<script language="javascript" type="text/javascript" src="info.js"></script>

</html>
