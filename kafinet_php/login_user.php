<?php
header("Content-Type: application/json");
require "config.php";

if ($_SERVER['REQUEST_METHOD'] == "POST") {
    $phone = $_POST['phone'];
    $password = $_POST['password'];

    $stmt = $conn->prepare("SELECT password FROM users WHERE phone = ?");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($hash);
        $stmt->fetch();
        if (password_verify($password, $hash)) {
            echo json_encode(["success" => true]);
        } else {
            echo json_encode(["success" => false, "error" => "wrong_password"]);
        }
    } else {
        echo json_encode(["success" => false, "error" => "not_found"]);
    }

    $stmt->close();
    $conn->close();
}
?>
