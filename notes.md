PREVIOUS CART HTML FILE
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Your Cart</title>
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    <!-- After editing style.css -->
    <link rel="stylesheet" href="/resources/css/style.css?v=1.0.2">
    
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" integrity="sha512-papcAxPZOkZLKFcV0iM6zzJzX/VCYqu7y7P+MmfQUqsmLSNKJVi2XScqYtQbnUmZUdOHF+xd9oZhxw50YdI0yQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.cdnfonts.com/css/segoe-ui-4" rel="stylesheet">
 
    <style>
        /* Neon border and shadow effect */
        .neon-box {
            border: 1px solid #9c27b0;
            box-shadow: 0 0 10px #9c27b0;
            border-radius: 10px;
        }

        /* Quantity Buttons */
        .btn-outline-secondary {
            color: #9c27b0;
            border-color: #9c27b0;
        }
        .btn-outline-secondary:hover {
            background-color: #9c27b0;
            color: #fff;
        }

        /* Success button for checkout */
        .btn-success {
            background-color: #9c27b0;
            border-color: #9c27b0;
        }
        .btn-success:hover {
            background-color: #7b1fa2;
        }

        /* Danger button for remove */
        .btn-danger {
            background-color: #c62828;
            border-color: #c62828;
        }
        .btn-danger:hover {
            background-color: #b71c1c;
        }
        
        .neon-border {
            border: 2px solid #a259ff; /* Sleek purple */
            box-shadow: 0 0 10px #a259ff44, 0 0 20px #a259ff33;
            border-radius: 15px;
            transition: all 0.3s ease;
        }

        .neon-border:hover {
            box-shadow: 0 0 15px #a259ffaa, 0 0 30px #a259ff77;
        }
        
        .card:hover {
            transform: translateY(-5px);
            transition: transform 0.3s ease;
        }
        
        .btn-outline-light {
            border-color: #a259ff;
            color: #a259ff;
        }
        .btn-outline-light:hover {
            background-color: #a259ff;
            color: #fff;
        }
        
        h1{
            font-family: 'Segoe UI';
            text-align: center;
            font-size: 60px;
            padding-top: 10px;
            padding-bottom: 30px;
            border-bottom: 2px solid #a259ff;
        }

        body {
            background-color: #000000;
            color: #fff;
            font-family: 'Segoe UI';
            
        }
        
        .btn-decrease, .btn-increase, .btn-delete-item, .btn-success {
            display: inline-block !important;
            visibility: visible !important;
            opacity: 1 !important;
        }
 
    </style>

</head>
<body style="background-color: #0d0d0d; color: #e0e0e0;">
    <!-- Navbar -->
    <div th:insert="fragments/navbar :: navbar"></div>
    <h1 class="mb-4 pt-4 pb-4 mt-4 mb-4"><span><i class="fa-solid fa-cart-shopping" style="color: #ffffff;"></i></span></h1>
<div class="container mt-4">
    
    

    <!-- Empty Cart Message -->
    <div th:if="${cartItems == null or #lists.isEmpty(cartItems)}">
        <div class="alert alert-info" style="font-size: 20px; text-align: center; font-weight: bold ">Your Cart is Empty...</div>
    </div>

    <!-- Cart cards -->
    <div th:if="${cartItems != null and !#lists.isEmpty(cartItems)}">
        <div class="row row-cols-1 row-cols-md-2 g-4">
            <div class="col" th:each="item : ${cartItems}">
                <div class="card h-100 shadow neon-border bg-dark text-white">
                    <div class="card-body">
                        <h5 class="card-title" th:text="${item.book.title}"></h5>
                        <h6 class="card-subtitle mb-2 text-muted" th:text="${item.book.author}"></h6>
                        <p class="card-text item-price">
                            <strong>Price:</strong> ₹<span th:text="${item.book.price}"></span><br>
                        </p>
                        <p class="card-text item-subtotal">    
                            <strong>Total:</strong> ₹<span th:text="${item.quantity * item.book.price}"></span>
                        </p>

                        <!-- Quantity controls -->
                        <div class="d-flex align-items-center">
                            <button class="btn btn-sm btn-outline-light btn-decrease" th:attr="data-item-id=${item.id},data-max=${item.book.copies}">−</button>
                            <input type="text"
                                th:id="'qty-' + ${item.id}"
                                class="mx-3 form-control qty-input text-center"
                                th:value="${item.quantity}"
                                readonly />
                            <button class="btn btn-sm btn-outline-light btn-increase" th:attr="data-item-id=${item.id},data-max=${item.book.copies}">+</button>
                        </div>

                        <!-- Stock warning -->
                        <div class="text-danger small mt-2" th:if="${item.quantity >= item.book.copies}">
                            Only <span th:text="${item.book.copies}"></span> left in stock!
                        </div>
                    </div>

                    <!-- delete item from cart -->
                    <div class="card-footer border-top border-secondary d-flex justify-content-end">
                        <button class="btn btn-sm btn-outline-light btn-delete-item" th:attr ="data-item-id=${item.id}">
                            <span><i class="fa-solid fa-trash"></i></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>


  
        


        
    
    <!-- Total Price -->
        <div class="mt-5 text-end">
            <h4>Total Amount: ₹<span th:text="${totalAmount}"></span></h4>
        </div>

        <!-- Checkout Button -->
        <div class="text-end mt-3">
            <form th:action="@{/checkout}" method="get">
                <button type="submit" class="btn btn-success" th:disabled="${hasOutOfStock}">
                    Proceed to Checkout
                </button>
            </form>
        </div>
    
    
    <!-- Optional Warnings (global) -->
        <div th:if="${outOfStockWarnings != null and !#lists.isEmpty(outOfStockWarnings)}"
             class="alert alert-danger mt-3">
            <ul class="mb-0">
                <li th:each="msg : ${outOfStockWarnings}" th:text="${msg}"></li>
            </ul>
        </div>
    </div>
    
    
    
    
</div>

 
<!-- Footer -->
    <div class="footer text-center">
        &copy; 2025 404BooksNotFound &nbsp;|&nbsp; Built with irony, styled with restraint.
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://kit.fontawesome.com/752b42e49f.js" crossorigin="anonymous"></script>
    <script th:src="@{/js/bootstrap.bundle.min.js}"></script>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>   
    
    
    
    





<script type="text/javascript" th:inline="javascript">
/*<![CDATA[*/

document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    // function to update quantity
    function updateQuantity(itemId, quantity){
        fetch(/*[[@{/cart/update}]]*/ '/cart/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: `itemId=${encodeURIComponent(itemId)}&quantity=${encodeURIComponent(quantity)}`
        })
        .then(response =>{
            if(!response.ok){
                return response.text().then(text => {throw new Error(text || "Failed to update"); });
            }
            return response.text();
        })
        .then(()=>{
            // Reload the page to update totals and UI
            window.location.reload();
        })
        .catch(err => Swal.fire("Error", err.message || "An error occurred.", "error"));       
    }
    
  // Increase Quantity
    document.querySelectorAll(".btn-increase").forEach(function (button) {
        
        button.addEventListener("click", function () {
            const input = this.parentElement.querySelector(".qty-input");
            const max = parseInt(this.getAttribute("data-max"));
            const itemId = this.getAttribute("data-item-id");
            const current = parseInt(input.value) || 1;
            
            if (current < max) {
                input.value = current + 1; // as soon as quantity is increased
                updateQuantity(itemId, current+1);    
            } else {
                Swal.fire("Out of Stock", `Only ${max} copies available.`, "warning");
            }
        });
    });


    // Decrease Quantity
    document.querySelectorAll(".btn-decrease").forEach(function (button) {
        button.addEventListener("click", function () {
            const input = this.parentElement.querySelector(".qty-input");
            const itemId = this.getAttribute("data-item-id");
            const current = parseInt(input.value) || 1;
            

            if (current > 1) {
                input.value = current - 1;
                updateQuantity(itemId, current-1); 
            }
        });
    });  
    
    // delete item
    document.querySelectorAll(".btn-delete-item").forEach(function (button){
        button.addEventListener("click", function(){
            const itemId = this.getAttribute("data-item-id");
            
            Swal.fire({
                    title: 'Are you sure?',
                    text: "Do you want to remove this item from cart?",
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#9c27b0',
                    cancelButtonColor: '#c62828',
                    confirmButtonText: 'Yes, remove it!'
                }).then((result) => {
                    if (result.isConfirmed) {
                        fetch(/*[[@{/cart/remove}]]*/ '/cart/remove', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                                [csrfHeader]: csrfToken
                            },
                            body: `itemId=${encodeURIComponent(itemId)}`
                        })
                        .then(response => {
                            if (!response.ok) {
                                return response.text().then(text => { throw new Error(text || "Failed to remove"); });
                            }
                            return response.text();
                        })
                        .then(() => {
                            Swal.fire("Removed!", "Item removed from cart.", "success")
                                .then(() => window.location.reload());
                        })
                        .catch(err => Swal.fire("Error", err.message || "An error occurred.", "error"));
                    }
                });
        });
    });
});

/*]]>*/
</script>




</body>
</html>

```