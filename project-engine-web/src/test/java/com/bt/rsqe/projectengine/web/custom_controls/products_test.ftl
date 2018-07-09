<@cc.product_list products=model attrs={"id":"1"}; productId>
${"prefix-\"" + productId}
</@cc.product_list>

<@cc.product_list model;  productId>
${productId}
</@cc.product_list>

<@cc.product_list products=model attrs={"id":"2"} selected="prefix-\"id2"; productId>
${"prefix-\"" + productId}
</@cc.product_list>