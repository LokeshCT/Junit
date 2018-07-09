(function (factory) {
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);
  } else {
    // Browser globals
    factory(jQuery);
  }
}(function ($){
	var methods = {
		globals : {
			timer:null,
			count:0
		},
		option:null,
		init: function(options) {
			methods.option = $.extend({
				message: '',
				source:'',
				rootLabel: 'Products'
			}, options || {});
			methods._create(this);
			return this;
		},
		_getProductNode: function(data, prodNode, table){
			//Extracting Attributes and creating table
			if(typeof data.Attributes != "undefined" && data.Attributes.length > 0){
				prodNode.append(methods._getAttributeNode(data.Attributes, table));
			}
			//Extracting Price Lines
			if(typeof data.PriceLines != "undefined" && data.PriceLines.length > 1){
				prodNode.append(methods._getPriceLineNode(data.PriceLines, table));
			}
			//Extracting Cost Lines
			if(typeof data.CostLines != "undefined" && data.CostLines.length > 1){
				prodNode.append(methods._getCostLineNode(data.CostLines, table));
			}
			//Extracting the related Products
			if(typeof data.RelatedProducts != "undefined" && data.RelatedProducts.length > 0){
				prodNode.append(methods._getRelatedProductNode(data.RelatedProducts, table));
			}
			return prodNode;
		},
		_getRelatedProductNode: function(data, table){
			var relProdNode = $('<li/>').append('<span class="nodeLabel">Related Products</span>').addClass('products');
			if(typeof data != "undefined" && data.length > 0){
				$.each(data,function(i,m){
					var childProdNode = $('<ul/>').append('<span class="nodeLabel">'+m[0].name+'</span>').addClass('products');
					relProdNode.append(methods._getProductNode(m[0], childProdNode, table));
				});
			}
			return relProdNode;
		},
		_getAttributeNode: function(data, table){
			var attrNode = $('<li/>').append('<span class="nodeLabel">Attributes</span>').addClass('detPar'),attrTab = table.clone();
			$.each(data,function(i,y){
				var tr = '<tr>';
				for(var i in y) {
					tr+='<td class="label">'+y[i].name+'</td><td>'+y[i].value+'</td>';
				}
				tr+='</tr>';
				attrTab.append(tr);
			});
			attrNode.append(attrTab);
			return attrNode;
		},
		_getPriceLineNode: function(data, table){
			var plNode = $('<li/>').append('<span class="nodeLabel">Price Lines</span>').addClass('detPar'),plTab = table.clone();
			$.each(data,function(i,y){
				if(i == 0){
					var th = '<th>';
					for(var i in y) {
						th+=y[i].name+'</th><th>'+y[i].otcGross+'</th><th>'+y[i].otcDisc+'</th><th>'+y[i].otcNet+'</th><th>'+y[i].rcGross+'</th><th>'+y[i].rcDisc+'</th><th>'+y[i].rcNet;
					}
					th+='</th>';
					plTab.append(th);
				}
				else{
					var tr = '<tr>';
					for(var i in y) {
						tr+='<td class="label">'+y[i].name+'</td><td>'+y[i].otcGross+'</td><td>'+y[i].otcDisc+'</td><td>'+y[i].otcNet+'</td><td>'+y[i].rcGross+'</td><td>'+y[i].rcDisc+'</td><td>'+y[i].rcNet;
					}
					tr+='</tr>';
					plTab.append(tr);
				}
			});
			plNode.append(plTab);
			return plNode;
		},
		_getCostLineNode: function(data, table){
			var clNode = $('<li/>').append('<span class="nodeLabel">Cost Lines</span>').addClass('detPar'),clTab = table.clone();
			$.each(data,function(i,y){
				if(i == 0){
					var th = '<th>';
					for(var i in y) {
						th+=y[i].name+'</th><th>'+y[i].otcGross+'</th><th>'+y[i].otcDisc+'</th><th>'+y[i].otcNet+'</th><th>'+y[i].rcGross+'</th><th>'+y[i].rcDisc+'</th><th>'+y[i].rcNet;
					}
					th+='</th>';
					clTab.append(th);
				}
				else{
					var tr = '<tr>';
					for(var i in y) {
						tr+='<td class="label">'+y[i].name+'</td><td>'+y[i].otcGross+'</td><td>'+y[i].otcDisc+'</td><td>'+y[i].otcNet+'</td><td>'+y[i].rcGross+'</td><td>'+y[i].rcDisc+'</td><td>'+y[i].rcNet;
					}
					tr+='</tr>';
					clTab.append(tr);
				}
			});
			clNode.append(clTab);
			return clNode;
		},
		_create: function(element){
			var parent= $('<ul/>').addClass('rsTree parent').append('<span class="nodeLabel">'+methods.option.rootLabel+'</span>'),self=this;
			var table = $('<table class="detailTab"></table>');
			if(typeof methods.option.source.products != "undefined"){
				$.each(methods.option.source.products,function(i,d){
					//Extracting sites
					var product = d[0],frontCatalogueProductNode = $('<li/>').append('<span class="nodeLabel">'+d[0].name+'</span>').addClass('sites');
					$.each(product.Sites,function(i,k){
						var siteNode = $('<ul/>').append('<span class="nodeLabel">'+k[0].name+' ('+k[0].address+')</span>').addClass('products');
						//Extracting the Product under sites
						$.each(k[0].Products, function(i,x){
							//Extracting Attributes and creating table
							if(x[0].Attributes != "undefined" && x[0].Attributes.length > 0){
								siteNode.append(methods._getAttributeNode(x[0].Attributes, table));
							}
							//Extracting Price Lines
							if(typeof x[0].PriceLines != "undefined" && x[0].PriceLines.length > 1){
								siteNode.append(methods._getPriceLineNode(x[0].PriceLines, table));
							}
							//Extracting Cost Lines
							if(typeof x[0].CostLines != "undefined" && x[0].CostLines.length > 1){
								siteNode.append(siteNode.append(methods._getCostLineNode(x[0].CostLines, table)));
                            }

							//Extracting the related Products
							if(typeof x[0].RelatedProducts != "undefined" && x[0].RelatedProducts.length > 0){
								siteNode.append(methods._getRelatedProductNode(x[0].RelatedProducts, table));
							}
							frontCatalogueProductNode.append(siteNode);
						});
						//frontCatalogueProductNode.append(siteNode);
					});
					parent.append(frontCatalogueProductNode);
				});
			}
			element.append(parent);
			this._attachEvents(element);
		},
		_attachEvents: function(element) {
			element.find('ul.rsTree span.nodeLabel').click( function() {
				var $this = $(this),$parent = $this.parent();
				if($parent.hasClass('parent')) {
					$this.toggleClass('expanded');
				} else if ($parent.hasClass('sites')) {
					$this.toggleClass('expanded');
				} else if ($parent.hasClass('products')) {
					$this.toggleClass('expanded');
				} else if ($parent.hasClass('detPar')) {
					$this.toggleClass('expanded');
					$this.siblings().toggle();
				}
			});
		},
		destroy: function() {
            this.find('ul.rsTree').remove();
		}
	};
	
	$.fn.getSitesByProductTree = function(method){
		// Method calling logic
		if (methods[method] && method.charAt(0) != '_') {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' +  method + ' does not exist in this plugin.');
		}
	};
}));
