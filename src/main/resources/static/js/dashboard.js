// 로그 출력
function addLog(message, type = 'info') {
    const logContainer = document.getElementById('logContainer');
    const timestamp = new Date().toLocaleTimeString();
    const logEntry = document.createElement('div');
    logEntry.className = `log-entry log-${type}`;
    logEntry.innerHTML = `<span class="log-timestamp">[${timestamp}]</span>${message}`;
    logContainer.appendChild(logEntry);
    logContainer.scrollTop = logContainer.scrollHeight;
}

// 로그 지우기
function clearLog() {
    document.getElementById('logContainer').innerHTML = '';
}

// 주문 생성 (단일)
async function createOrder(version) {
    const userId = document.getElementById('userId').value;
    const productId = document.getElementById('productId').value;
    const quantity = document.getElementById('quantity').value;
    const couponId = document.getElementById('couponId').value;
    const pointsToUse = document.getElementById('pointsToUse').value;

    const request = {
        userId: parseInt(userId),
        items: [{
            productId: parseInt(productId),
            quantity: parseInt(quantity)
        }],
        couponId: couponId ? parseInt(couponId) : null,
        pointsToUse: parseInt(pointsToUse)
    };

    addLog(`${version.toUpperCase()}로 주문 생성 요청 시작...`, 'info');

    try {
        const response = await fetch(`/api/${version}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request)
        });

        if (response.ok) {
            const data = await response.json();
            addLog(`✅ ${version.toUpperCase()} 주문 생성 성공! 주문 ID: ${data.orderId}, 최종 금액: ${data.finalAmount}원`, 'success');
        } else {
            const error = await response.text();
            addLog(`❌ ${version.toUpperCase()} 주문 생성 실패: ${error}`, 'error');
        }
    } catch (error) {
        addLog(`❌ ${version.toUpperCase()} 주문 생성 에러: ${error.message}`, 'error');
    }

    // 데이터 새로고침
    setTimeout(() => refreshData(), 1000);
}

// 주문 생성 (동시 요청)
async function createOrderConcurrent(version) {
    const concurrentRequests = parseInt(document.getElementById('concurrentRequests').value);
    const userId = document.getElementById('userId').value;
    const productId = document.getElementById('productId').value;
    const quantity = document.getElementById('quantity').value;
    const couponId = document.getElementById('couponId').value;
    const pointsToUse = document.getElementById('pointsToUse').value;

    const request = {
        userId: parseInt(userId),
        items: [{
            productId: parseInt(productId),
            quantity: parseInt(quantity)
        }],
        couponId: couponId ? parseInt(couponId) : null,
        pointsToUse: parseInt(pointsToUse)
    };

    addLog(`⚡ ${version.toUpperCase()}로 ${concurrentRequests}개의 동시 요청 시작...`, 'warning');

    const promises = [];
    for (let i = 0; i < concurrentRequests; i++) {
        const promise = fetch(`/api/${version}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request)
        }).then(async response => {
            if (response.ok) {
                const data = await response.json();
                return { success: true, data };
            } else {
                const error = await response.text();
                return { success: false, error };
            }
        }).catch(error => {
            return { success: false, error: error.message };
        });
        promises.push(promise);
    }

    const results = await Promise.all(promises);

    const successCount = results.filter(r => r.success).length;
    const failCount = results.filter(r => !r.success).length;

    addLog(`📊 ${version.toUpperCase()} 동시 요청 완료 - 성공: ${successCount}, 실패: ${failCount}`,
        successCount === concurrentRequests ? 'success' : 'warning');

    if (failCount > 0) {
        addLog(`⚠️  실패 원인: ${results.filter(r => !r.success).map(r => r.error).join(', ')}`, 'error');
    }

    // 데이터 새로고침
    setTimeout(() => refreshData(), 1000);
}

// 주문 취소
async function cancelOrder(orderId, version) {
    addLog(`${version.toUpperCase()}로 주문 취소 요청: 주문 ID ${orderId}`, 'info');

    try {
        const response = await fetch(`/api/${version}/orders/${orderId}/cancel`, {
            method: 'POST'
        });

        if (response.ok) {
            addLog(`✅ ${version.toUpperCase()} 주문 취소 성공! 주문 ID: ${orderId}`, 'success');
        } else {
            const error = await response.text();
            addLog(`❌ ${version.toUpperCase()} 주문 취소 실패: ${error}`, 'error');
        }
    } catch (error) {
        addLog(`❌ ${version.toUpperCase()} 주문 취소 에러: ${error.message}`, 'error');
    }

    // 데이터 새로고침
    setTimeout(() => refreshData(), 1000);
}

// 데이터 새로고침
async function refreshData() {
    addLog('🔄 데이터 새로고침 중...', 'info');

    try {
        // 상품 데이터
        const productsResponse = await fetch('/api/products');
        const products = await productsResponse.json();
        updateProductsTable(products);

        // 사용자 데이터
        const usersResponse = await fetch('/api/users');
        const users = await usersResponse.json();
        updateUsersTable(users);

        // 쿠폰 데이터
        const couponsResponse = await fetch('/api/coupons');
        const coupons = await couponsResponse.json();
        updateCouponsTable(coupons);

        // 주문 데이터
        const ordersResponse = await fetch('/api/orders');
        const orders = await ordersResponse.json();
        updateOrdersTable(orders);

        addLog('✅ 데이터 새로고침 완료', 'success');
    } catch (error) {
        addLog(`❌ 데이터 새로고침 실패: ${error.message}`, 'error');
    }
}

function updateProductsTable(products) {
    const tbody = document.getElementById('productsTable');
    tbody.innerHTML = '';
    products.forEach(product => {
        const row = `<tr>
            <td>${product.id}</td>
            <td>${product.name}</td>
            <td class="stock-count">${product.stockQuantity}</td>
            <td>${product.price.toLocaleString()}원</td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function updateUsersTable(users) {
    const tbody = document.getElementById('usersTable');
    tbody.innerHTML = '';
    users.forEach(user => {
        const row = `<tr>
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.grade}</td>
            <td class="point-count">${user.pointBalance.toLocaleString()}</td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function updateCouponsTable(coupons) {
    const tbody = document.getElementById('couponsTable');
    tbody.innerHTML = '';
    coupons.forEach(coupon => {
        const row = `<tr>
            <td>${coupon.id}</td>
            <td>${coupon.name}</td>
            <td>${coupon.usedCount}/${coupon.totalAvailableCount}</td>
            <td class="coupon-count">${coupon.remainingCount}</td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function updateOrdersTable(orders) {
    const tbody = document.getElementById('ordersTable');
    tbody.innerHTML = '';
    orders.forEach(order => {
        const statusClass = order.status === 'CONFIRMED' ? 'status-confirmed' : 'status-cancelled';
        const userId = order.userId || (order.user ? order.user.id : 'N/A');
        const row = `<tr>
            <td>${order.orderId || order.id}</td>
            <td>${userId}</td>
            <td class="${statusClass}">${order.status}</td>
            <td>${order.totalAmount.toLocaleString()}원</td>
            <td>${order.discountAmount.toLocaleString()}원</td>
            <td>${order.pointUsed.toLocaleString()}</td>
            <td>${order.finalAmount.toLocaleString()}원</td>
            <td>${new Date(order.orderDate).toLocaleString()}</td>
            <td>
                <button onclick="cancelOrder(${order.orderId || order.id}, 'v1')" class="btn btn-xs btn-danger">V1 취소</button>
                <button onclick="cancelOrder(${order.orderId || order.id}, 'v2')" class="btn btn-xs btn-success">V2 취소</button>
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

// 페이지 로드 시
window.onload = function() {
    addLog('🚀 대시보드 초기화 완료', 'success');
    addLog('💡 V1은 동시성 이슈가 발생하고, V2는 Redis 분산락으로 해결된 버전입니다.', 'info');
};
