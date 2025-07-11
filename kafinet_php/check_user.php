<?php
header("Content-Type: application/json");
require "config.php";

if ($_SERVER['REQUEST_METHOD'] == "POST") {
    $phone = $_POST['phone'];

    $stmt = $conn->prepare("SELECT id FROM users WHERE phone = ?");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        echo json_encode(["exists" => true]);
    } else {
        echo json_encode(["exists" => false]);
    }

    $stmt->close();
    $conn->close();
}
?>
