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