select * from orders;

SELECT o.order_number, o.customer_id, o.status, o.total_amount, i.sku, i.quantity, i.price
FROM orders o
JOIN order_items i ON o.id = i.order_id;


SELECT * FROM inventory;
SELECT sku, quantity, reserved_quantity 
FROM inventory 
WHERE sku = 'PROD-HEADPHONE-01';

