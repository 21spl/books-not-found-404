<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Checkout</title>
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    
    <!-- After editing style.css -->
<link rel="stylesheet" th:href="@{/css/style.css(v='1.0.2')}">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.cdnfonts.com/css/segoe-ui-4" rel="stylesheet">
<link href="https://fonts.cdnfonts.com/css/cascadia-code" rel="stylesheet">

<style>
        body {
            background-color: #000000;
            color: #ffffff;
            font-family: 'Segoe UI', sans-serif, 'Cascadia Code', 'Caveat Brush', 'Pastry Paradise', 'Cracksmoon', 'Playpen Sans';
            font-size: 1.3rem;
        }
        
        .card {
            border: 1px solid rgba(138, 43, 226, 0.4); /* Neon dark purple (like BlueViolet) */
            box-shadow: 0 0 8px rgba(138, 43, 226, 0.2); /* subtle glow */
            border-radius: 10px;
            transition: all 0.3s ease;
            background-color: #000000; /* optional for dark themes */
            color: #fff; /* optional: for text contrast */
            margin: 20px;
            padding-left: 40px;
            padding-right: 40px;
        }
        
        .footer {
            padding: 1rem;
            font-size: 1rem;
            background-color: #1a1a1a;
            border-top: 1px solid #2c2c2c;
            color: #888;
        }
        
        hr {
            border-color: #a259ff;
            border-width: 2px;
        }
        
        .cod-note {
            font-size: 1rem;
            margin-top: 10px;
            color: #ccc;
        }

        .alert-warning {
            background-color: #332200;
            color: #ffc107;
            border: 1px solid #664400;
        }
        
         h2, h4, label {
             padding-top: 30px;
            color: #a259ff;
        }

        .table-dark-custom {
            background-color: #000000;
            border-radius: 4px;
            overflow: hidden;
            margin: 10px;
            padding: 10px;
            color: #fff;
            font-family: 'Cascadia Code';
            font-size: 20px;
                
        }

        .table-dark-custom thead {
            background-color: #000000;
            color: #fff;
            border-color: #1B1F23;
        }

        .table-dark-custom tbody tr {
            border-color: #1B1F23;
        }

        .table-dark-custom td,
        .table-dark-custom th {
            background-color: transparent;
            color: #fff;
        }

        .table-dark-custom tbody tr:hover {
            background-color: #2a2a2a;
        }

        .form-control, textarea {
            background-color: #1a1a1a;
            color: #ffffff;
            border: 1px solid #444;
        }

        .form-control:focus, textarea:focus {
            background-color: #1a1a1a;
            color: #ffffff;
            border-color: #a259ff;
            box-shadow: 0 0 5px #a259ff66;
        }

        .btn-primary {
            background-color: #a259ff;
            border: none;
            padding-bottom: 10px;
        }

        .btn-primary:hover {
            background-color: #8a3edb;
        }
        
        .total-amt{
            color: #fff;
        }

</style>

</head>

<body>
    
    <!-- Navbar -->
    <div th:insert="fragments/navbar :: navbar"></div>
    
    
        <div class="container mt-5">
        <div class="card">
            <h2 class="mb-4">Order Summary</h2>

            <div th:if="${cartItems.size() == 0}">
                <div class="alert alert-warning">Your cart is empty. Please add items before checkout.</div>
            </div>

            <div th:if="${cartItems.size() > 0}">
                <table class="table table-bordered table-dark-custom">
                    <thead>
                        <tr>
                            <th>Book</th>
                            <th>Author</th>
                            <th>Price (₹)</th>
                            <th>Quantity</th>
                            <th>Total (₹)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="item : ${cartItems}">
                            <td th:text="${item.book.title}"></td>
                            <td th:text="${item.book.author}"></td>
                            <td th:text="${item.book.price}"></td>
                            <td th:text="${item.quantity}"></td>
                            <td th:text="${item.quantity * item.book.price}"></td>
                        </tr>
                    </tbody>
                </table>

                <h4 class="text-end mt-4 total-amt">Total Amount: ₹<span th:text="${totalAmount}"></span></h4>
                <p class="text-end cod-note">Accepted Payment Mode: <strong>Online Payment via Stripe</strong></p>


<!-- --------------------------- SHIPPING DETAILS --------------------------- -->
                <hr class="my-4" />
                <h2 class="mb-3">Shipping Details</h2>

                <form th:action="@{/checkout/placeOrder}" method="post" class="needs-validation" >
                    <div class="mb-3">
                        <label for="address" class="form-label">Shipping Address</label>
                        <textarea class="form-control" id="address" name="address" rows="3" required></textarea>
                        <div class="invalid-feedback">Please enter your shipping address.</div>
                    </div>

                    <div class="mb-3">
                        <label for="phone" class="form-label">Phone Number</label>
                        <input type="tel" class="form-control" id="phone" name="phone" pattern="[0-9]{10}" required>
                        <div class="invalid-feedback">Enter a valid 10-digit phone number.</div>
                    </div>

                    <div class="text-end pb-4">
                        <button type="submit" class="btn btn-primary">Place Order</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    
    
<!-- Footer -->
    <div class="footer text-center">
        &copy; 2025 404BooksNotFound &nbsp;|&nbsp; Built with irony, styled with restraint.
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        
</body>
</html>