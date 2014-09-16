String.prototype.capitalize=function(){
	return this.charAt(0).toUpperCase()+this.slice(1);
};
var titleEnhMap={'snatky':'sňatků','umrti':'úmrtí','narozeni':'narození','tesnovice':'Těšnovice','travnik':'Trávník','kmpm':'Kroměříž - Panny Marie','zlamanka':'Zlámanka','podoli':'Podolí','cechy':'Čechy','sisma':'Šišma','kladniky':'Kladníky','tucin':'Tučín','sobechleby':'Soběchleby','slezska':'Slezská'};
function oznac(id,area){
  show(id);
  var znak=document.getElementById('oznacenyZnak').style;
  var popis=document.getElementById('oznacenyPopis').style;
  var coords=area.coords.split(",");
  if(area.shape=='rect'){
    znak.left='0px';
    znak.top='0px';
    znak.width='0px';
    znak.height='0px';

    popis.left=coords[0]+'px';
    popis.top=coords[1]+'px';
    popis.width=(+coords[2]-coords[0])+'px';
    popis.height=(+coords[3]-coords[1])+'px';
  }else{
    znak.left=(+coords[0]+1)+'px';
    znak.top=(+coords[1]+1)+'px';
    znak.width=(+coords[2]-coords[0])+'px';
    znak.height=(+coords[5]-coords[1])+'px';

    popis.left=(+coords[12]+1)+'px';
    popis.top=(+coords[13]+1)+'px';
    popis.width=(+coords[6]-coords[12])+'px';
    popis.height=(+coords[9]-coords[5])+'px';
  }
}
function odznac(){
  show('vlastRodok');
  var znak=document.getElementById('oznacenyZnak').style;
  var popis=document.getElementById('oznacenyPopis').style;
  znak.left='0px';znak.top='0px';znak.width='0px';znak.height='0px';
  popis.left='0px';popis.top='0px';popis.width='0px';popis.height='0px';
}
function urlTrim(str,length,appendix){
	str=str.replace(/^\s*https?:\/\//,'');
	if(str.length<=length)return str;

	var trm=str.substr(0,length);
	var si=trm.indexOf('/');
	if(si>=0)trm=trm.substr(0,si);

	if(trm&&si==-1)trm+=appendix;
	return trm;
}
$.extend($.fn.linkify.plugins,{
	shortie:function(html){
		return html.replace(
			/(^|["'(\s]|&lt;)((?:(?:https?|ftp):\/\/|mailto:).+?)((?:[:?]|\.+)?(?:\s|$)|&gt;|[)"',])/g, 
			function(m){
				return ['<a target="_blank" href="',m,'">',urlTrim(m,30,' ...'),'</a>'].join('');
			}
		);
	}
});
jQuery.fn.replaceText=function(fn,str){
	var func=jQuery.isFunction(fn);
	this.contents().each(jwalk);

	function jwalk(){
		var nn=this.nodeName.toLowerCase();
		if(nn==='#text'){
			if(func){
				fn.call(this);
			}else{
				this.data=this.data.replace(fn, str);
			}
		}else if(this.nodeType===1&&this.childNodes&&this.childNodes[0]&&nn!=='script'&&nn!=='textarea'){
			$(this).contents().each(jwalk);
		}
	}
	return this;
};
function replaceShortcuts(objects){
	objects.each(function(i,obj){
		var t=$(obj).text();
		if(t in labelsMap){
			$(obj).text(labelsMap[t]);
		}else{
			for(a in labelsMap){
				$(obj).replaceText(a, labelsMap[a]);
			}
		}
	});
	return objects;
}
function isSpecialVztah(tr){
	return tr.has("td:contains('%S%')").size()>0;
}
function isPartner(tr){
	return tr.has("td:contains('%X')").size()>0;
}
function getDatumVzniku(tr){
	var datumVzniku='';
	var nextTR=tr.next();
	while(!isSpecialVztah(nextTR)&&!isPartner(nextTR)&&nextTR.text()!=''){
		var t=nextTR.text();
		if(/^%D%/.test(t)){
			datumVzniku=t.substr(3);
		}
		nextTR=nextTR.next();
	}
	return datumVzniku;
}
function handleImg(obj){
	obj.find("img").each(function(i){
		var anch=$(this).parent();
		anch.attr('href', subdir+anch.attr('href'));
		$(this).attr('src',anch.attr('href').replace('.','_mini.'));
	});
}
function show(id){
  var pd=$('#personDetails');
  pd.html($('#'+id).html());
  handleImg(pd);
  
  if (id!='vlastRodok'){
		pd.prepend('<tr><td colspan="2" class="p" style="padding-top:7px;">Osobní info</td></tr>');
	}
  pd.find("tr").has("td:contains('%X')").not(":first").each(function(i){
  	if($(this).prev().has("td:contains('%S%')").size()==0){
			$(this).before('<tr><td colspan="2"><hr/></td></tr>');
		}
	});
	pd.linkify({
		use:'shortie',
		handleLinks:function(links){
			links.addClass('linkified');
		}
	});
	if(pd.find("a[title*='¨S']").size()==0){
		var jmeno=pd.find("tr").has("td:contains('%J')").find('td:nth-child(2)').text();
		var prijmRod=pd.find("tr").has("td:contains('%R')").find('td:nth-child(2)').text();
		var prijm=pd.find("tr").has("td:contains('%B')").find('td:nth-child(2)').text();
		var celeJmeno=prijmRod?[jmeno,prijmRod,prijm].join(' '):[jmeno,prijm].join(' ');
		pd.find("tr").has("td:contains('%X')").each(function(i){
			if(!isSpecialVztah($(this).prev())){
				var vztahyMapKey=celeJmeno.concat('%D%', getDatumVzniku($(this)));
				if(vztahyMapKey in vztahyMap){
					var vztah=vztahyMap[vztahyMapKey];
					var searched=$('#'+vztah);
					if(searched.find("tr").has("td:contains('%X')").has("td:contains('"+celeJmeno+"')").size()==1){
						var searchedSnatky=searched.find("tr:has(a[title*='¨S'])");
						if(searchedSnatky.size()>0){
							console.log('osoba '+vztah+' ma snatek a vztah se zobrazovanou osobou '+id+', jeji snatek je zkopirovan');
							var clone=searchedSnatky.clone();
							handleImg(clone);
							
							if(pd.find("tr:has(a[title*='¨N'])").after(clone).size()==0){
								if(pd.find("tr:has(a[title*='¨U'])").before(clone).size()==0){
									if(pd.find("tr:has(td:contains('dokument')[class='p'])").after(clone).size()==0){
										pd.append(clone);
									}else{
										console.log("NEBYLO KAM PRIDAT KLON "+celeJmeno);
									}
								}
							}
						}
					}
				}
			}
		});
	}
	pd.find("a[title*='¨N']").before('<img src="img/narozeni.png" class="lifeIcon" width="30">');
	pd.find("a[title*='¨S']").before('<img src="img/snatky.png" class="lifeIcon" width="30">');
	pd.find("a[title*='¨U']").before('<img src="img/umrti.png" class="lifeIcon" width="30">');
	pd.find("a:has(img)").each(function(i,anch){
		var tit=$(anch).attr('title');
		for(var t in titlesMap){
			tit=tit.replace(t,titlesMap[t]);
		}
		$(anch).attr('title',tit);
	});
	replaceShortcuts(pd.find("td, div"));
	
	var cnt=pd.find("img[class!='lifeIcon']").size();
	var apx=cnt>1?(cnt>4?'ů':'y'):'';
	pd.find("img").first().parents("tr").before(['<tr><td colspan="2" class="p">',cnt,' dokument',apx,'</td></tr>'].join(''));
	pd.find('.p').wrapInner('<span class="h"></span>');
	pd.find("td:not([colspan]):first-child").attr('class','l');
	pd.find("td[colspan]:has(img)").attr('class','c');
	pd.find('a:has(img)').attr('rel', 'gallery').fancybox({
		padding:5,
		margin:[25,20,2,2],
		minHeight:10,
		minWidth:10,
		openEffect:'none',
		closeEffect:'none',
		closeEasing:'none',
		closeClick:true,
		loop:false,
		helpers:{
			overlay:{
				speedOut:100
			},
			title:{
				type:'float'
			}
		},
		beforeClose:fancyBeforeClose,
		afterLoad:fancyAfterLoad,
		tpl:{
			closeBtn:'<a title="Zavřít (ESCAPE)" class="fancybox-item fancybox-close" href="javascript:;"></a>',
			next:'<a title="Další" class="fancybox-nav fancybox-next" href="javascript:;"><span></span></a>',
			prev:'<a title="Předchozí" class="fancybox-nav fancybox-prev" href="javascript:;"><span></span></a>'
		}
	});
}
function fancyAfterLoad() {
	var list=$("#links");
	if(!list.length) {    
		list=$('<ul id="links">');
		for(var i=0;i<this.group.length;i++){
			$('<li data-index="'+i+'"><label></label></li>').click(function(){$.fancybox.jumpto($(this).data('index'));}).appendTo(list);
		}
		list.appendTo('body');
  }
  list.find('li').removeClass('active').eq(this.index).addClass('active');
    
	this.title=this.title
		.replace(/^(mzab|zao)-(\w+)-(\d+)-(narozeni|snatky|umrti)-([0-9]+)_([0-9]+)!*/, function(m,p1,p2,p3,p4,p5,p6){
			var res="<span style='color:#aaa'>"+(p1=='mzab'?'Moravský zemský archiv Brno':'Zemský archiv Opava')+"</span>";
			res+=" &#45; X"+p4.toUpperCase()+"X";
			res+="Kniha "+p4;
			res+=" "+p2.replace(/_([a-z])/g,function(n,q1){return q1.toUpperCase()}).capitalize();
			res+=" #"+p3+" <span style='color:#aaa'>["+p5+"/"+p6+"]</span>:";
			return res;
		})
		.replace(/([a-z])([A-Z,0-9])/g,'$1 $2')
		.replace(/-|_/g,' ');
	var pd=$('#personDetails');
	var jmeno=pd.find("tr").has("td:contains('Jméno')").find('td:nth-child(2)').text();
	var prijmRod=pd.find("tr").has("td:contains('Rodné příjmení')").find('td:nth-child(2)').text();
	var prijm=pd.find("tr").has("td:contains('Příjmení')").find('td:nth-child(2)').text();
	var partners=pd.find("td:has(div:contains('Partner'))").text().replace(/Partner/g,' ');
	var partners2="";
	pd.find("td:contains('Partner')").next().each(function(i){
		partners2+=' '+$(this).text();
	}); 
	var dia=(jmeno+' '+prijmRod+' '+prijm+' '+partners+' '+partners2).split(' ');
	var words=this.title.split(' ');
	for(var x=0;x<words.length;x++){
		for(var y=0;y<dia.length;y++){
			if(words[x].toLowerCase()==remDiac(dia[y]).toLowerCase()){
				this.title=this.title.replace(new RegExp(words[x],'g'),dia[y]);
				break;
			}
		}
		if(words[x].toLowerCase() in titleEnhMap){
			this.title=this.title.replace(new RegExp(words[x],'gi'),titleEnhMap[words[x].toLowerCase()]);
		}
	}
	this.title=this.title.replace('XnarozeníX', '<img src="img/narozeni-w.png" style="height:1.3em;margin-bottom:0;border:0;"/>');
	this.title=this.title.replace('XsňatkůX', '<img src="img/snatky-w.png" style="height:1.3em;margin-bottom:0;border:0;"/>');
	this.title=this.title.replace('XúmrtíX', '<img src="img/umrti-w.png" style="height:1.3em;margin-bottom:0;border:0;"/>');
}
function fancyBeforeClose(){
	$("#links").remove();    
}
var sdiak="áäčďéěíĺľňóôőöŕšťúůűüýřžÁÄČĎÉĚÍĹĽŇÓÔŐÖŔŠŤÚŮŰÜÝŘŽ";
var bdiak="aacdeeillnoooorstuuuuyrzAACDEEILLNOOOORSTUUUUYRZ";
function remDiac(s){
	var tx=""; var txt=s;
	for(var p=0;p<txt.length;p++){
		if(sdiak.indexOf(txt.charAt(p))!=-1)
			tx+=bdiak.charAt(sdiak.indexOf(txt.charAt(p)));
		else tx+=txt.charAt(p);
	}
	return tx;
}
function imgLoad(imgWidth,imgHeight){
  var rodokmen=document.getElementById('right');
  rodokmen.scrollLeft=imgWidth/2 - rodokmen.clientWidth/2;
  rodokmen.scrollTop=imgHeight/2 - rodokmen.clientHeight/2;
}
function showChanges() {
	var ch=$("#lastChanges");
	ch.dialog({
		autoOpen:false,
		show:{effect:'slide',duration:250},
		hide:{effect:'slide',duration:150},
		height:"300",
		width:"auto",
		position:[0,18],
		open:function(){$(".ui-dialog").css("box-shadow","#999 1px 4px 6px");$('.ui-dialog-titlebar-close').attr('title', 'Zavřít (ESCAPE)')}
	});
	ch.dialog(ch.dialog("isOpen")?"close":"open");
}
$().ready(function(){
	$("#rodSplitter").splitter({
		type:"v",
		outline:false,
		minLeft:100,sizeLeft:300,minRight:100,
		resizeToWidth:true,
		cookie:"vsplitter",
		accessKey:'I'
	});
	$('body').bind('click',function(e){
		if($('#lastChanges').dialog('isOpen')&&!$(e.target).is('.ui-dialog, a')&&!$(e.target).closest('.ui-dialog').length){
			$('#lastChanges').dialog('close');
		}
	});
	$('area').each(function(i){
  	$(this).attr('href','javascript:;');
  	$(this).attr('onclick',"oznac(".concat(i,",this)"));
  	$(this).attr('shape', $(this).attr('coords').split(",").length==4?'rect':'poly' );
	});

});