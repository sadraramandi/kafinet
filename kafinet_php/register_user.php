<?php
header("Content-Type: application/json");
require "config.php";

if ($_SERVER['REQUEST_METHOD'] == "POST") {
    $phone = $_POST['phone'];
    $password = $_POST['password'];
    $hash = password_hash($password, PASSWORD_DEFAULT);

    $stmt = $conn->prepare("INSERT INTO users (phone, password) VALUES (?, ?)");
    $stmt->bind_param("ss", $phone, $hash);

    if ($stmt->execute()) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "error" => $stmt->error]);
    }

    $stmt->close();
    $conn->close();
}
?>
