<!DOCTYPE html>
<html lang="en">
<head>
<title>Design OCR coordinates</title>
<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.4/jquery.min.js"></script>
<script src="${contextPath}/js/jquery.Jcrop.js"></script>
<style type="text/css">

input.textBox {
	margin-top: 5px;
	margin-bottom: 5px;
	height: 30px;
	width: 350px;
	font-size: 15px;
	font-family: Verdana;
	line-height: 30px;
	display: inline-block;
	*display: inline; /* for IE7*/
	zoom: 1; /* for IE7*/
	vertical-align: middle;
}

label.inputLabel {
	font-family: Verdana;
	font-size: 15px;
	line-height: 30px;
	display: inline-block;
	*display: inline; /* for IE7*/
	zoom: 1; /* for IE7*/
}

.btn-file {
	position: relative;
	overflow: hidden;
}

.btn-file input[type=file] {
	position: absolute;
	top: 0;
	right: 0;
	min-width: 100%;
	min-height: 100%;
	font-size: 100px;
	text-align: right;
	filter: alpha(opacity = 0);
	opacity: 0;
	outline: none;
	cursor: inherit;
	display: block;
}
</style>
<script type="text/javascript">
	function previewFile(input) {
		if ($('#previewImg').data('Jcrop')) {
			$('#previewImg').data('Jcrop').destroy();
		}
		var file = $("input[type=file]").get(0).files[0];

		if (file) {
			if(file.type == "application/pdf"){
				var formData = new FormData();
				formData.append('file', $('input[type=file]')[0].files[0]);
				
				$.ajax({
		            type: "POST",
		            enctype: 'multipart/form-data',
		            url: "../api/ocr/upload",
		            data: formData,
		            processData: false,
		            contentType: false,
		            cache: false,
		            timeout: 600000,
		            success: function (data) {  
		            	
		                console.log("SUCCESS : ", data.message);
		            	$("#previewImg").attr("src", "data:image/png;base64,"+data.message);
						initializeJcrop();
						$("#imgDiv").show();

		            },
		            error: function (e) {

		              
		                console.log("ERROR : ", e);
		              

		            }
		        });
				
				
			}
			else{
				var reader = new FileReader();

				reader.onload = function() {
					
					$("#previewImg").attr("src", reader.result);
					initializeJcrop();
					$("#imgDiv").show();
				}

				reader.readAsDataURL(file);
			}
			
		}

	}
	function initializeJcrop() {
		var jcrop_api;

		$('#previewImg').Jcrop({
			onChange : showCoords,
			onSelect : showCoords,
			onRelease : clearCoords
		}, function() {
			jcrop_api = this;
		});

		$('#coords').on(
				'change',
				'input',
				function(e) {
					var x1 = $('#x1').val(), x2 = $('#x2').val(), y1 = $('#y1')
							.val(), y2 = $('#y2').val();
					jcrop_api.setSelect([ x1, y1, x2, y2 ]);
				});

	}
	
	// Simple event handler, called from onChange and onSelect
	// event handlers, as per the Jcrop invocation above
	function showCoords(c) {
		$('#x1').val(c.x);
		$('#y1').val(c.y);
		$('#x2').val(c.x2);
		$('#y2').val(c.y2);
		$('#w').val(c.w);
		$('#h').val(c.h);
	};

	function clearCoords() {
		$('#coords input').val('');
	};
	
	
	
	
</script>

<script type="text/javascript">
var obj = {};
var counter = 2;
$(document).ready(function(){
	
  
        
    $("#addButton").click(function () {
                
    if(counter>8){
            alert("Only 8 textboxes allow");
            return false;
    }   
        
    var newTextBoxDiv = $(document.createElement('div'))
         .attr("id", 'TextBoxDiv' + counter);
                
    newTextBoxDiv.after().html('<label class="inputLabel">Field #'+ counter + ' : </label>' +
          '<input placeholder="Enter Field Name" class="textBox" type="text" name="textbox' + counter + 
          '" id="textbox' + counter + '" value="" > <label class="inputLabel">Value #'+ counter + ' : </label>' +
          '<input placeholder="Enter X,Y,W,H coordinates" class="textBox" type="text" name="textbox' + counter + 
          '" id="textValue' + counter + '" value="" >');
  
   
    newTextBoxDiv.appendTo("#TextBoxesGroup");

                
    counter++;
     });

     $("#removeButton").click(function () {
    if(counter==1){
          alert("No more textbox to remove");
          return false;
       }   
        
    counter--;
            
        $("#TextBoxDiv" + counter).remove();
            
     });
        
  });
  
function getButtonValue() {
    
    var msg = '';
	
    for(i=1; i<counter; i++){
   	  msg += "\n FieldName #" + i + " : " + $('#textbox' + i).val();
   	  msg += "\n FieldValue #" + i + " : " + $('#textValue' + i).val();
   
   	obj[$('#textbox' + i).val()] = $('#textValue' + i).val();
 
    }
 
}

function saveOcrCoordMapping(){
	getButtonValue();
	$.ajax({
	      type: 'POST',
	      contentType: "application/json; charset=utf-8",
	      url: "../api/ocr/save/"+$("#templateName").val(),
	      data: JSON.stringify(obj),
	      success: function(resultData) { 
	    	  if(resultData=="success"){
	    		  alert("Save Completed");
	    		  location.reload();
	    	  }
	    	  else{
	    		  alert(resultData);
	    	  }
	    	  
	      }
	});
	
}

  
</script>
<link rel="stylesheet" href="${contextPath}/demo_files/main.css"
	type="text/css" />
<link rel="stylesheet" href="${contextPath}/demo_files/demos.css"
	type="text/css" />
<link rel="stylesheet" href="${contextPath}/css/jquery.Jcrop.css"
	type="text/css" />

</head>
<body onload=initializeJcrop();>

	<div class="container">
		<div class="row">
			<div class="span12">
				<div class="jc-demo-box">

					<div class="page-header">
						<ul class="breadcrumb first">

							<li class="active">Design OCR coordinates</li>
						</ul>
						<h1>Design OCR coordinates</h1>
					</div>
					<h4>Create OCR Template Name</h4>
					<div>
						<input type="text" id="templateName"
							placeholder="Enter OCR Template Name" />
					</div>
					<h4>Choose File to upload</h4>
					<div>
						<span class="btn btn-primary btn-file"> Browse <input
							type="file" id="myPdf" class="form-control"
							accept="image/jpeg,image/gif,image/png,application/pdf,image/x-eps"
							onchange="previewFile(this);" required /><br>
						</span>
					</div>
					<br />
					<div id="imgDiv" style="display: none">
						<div>
							<img src="${contextPath}/demo_files/sago.jpg" id="previewImg"
								alt="Pan card Example]" />
						</div>

						<!-- This is the form that our event handler fills -->
						<form id="coords" class="coords" onsubmit="return false;"
							action="http://example.com/post.php">

							<div class="inline-labels">
								<label>X <input type="text" size="4" id="x1" name="x1" /></label>
								<label>Y <input type="text" size="4" id="y1" name="y1" /></label>
								<label style="display: none">X2 <input type="text"
									size="4" id="x2" name="x2" /></label> <label style="display: none">Y2
									<input type="text" size="4" id="y2" name="y2" />
								</label> <label>W <input type="text" size="4" id="w" name="w" /></label>
								<label>H <input type="text" size="4" id="h" name="h" /></label>

							</div>

						</form>

						<h3>OCR coordinates Mapping</h3>
						<div id='TextBoxesGroup'>
							<div id="TextBoxDiv1">
								<label class="inputLabel">Field #1 : </label><input
									placeholder="Enter Field Name" class="textBox" type='text'
									id='textbox1'> <label class="inputLabel">Value
									#1 : </label><input placeholder="Enter X,Y,W,H coordinates"
									class="textBox" type='text' id='textValue1'>
							</div>
						</div>
						<button type="button" value='Add Field' id='addButton'
							class="btn btn-primary">Add Field</button>
						<button type="button" value='Remove Field' id='removeButton'
							class="btn btn-danger">Remove Field</button>
						<div class="clearfix"></div>

						<button type="button" style="margin-left: 400px"
							onclick="saveOcrCoordMapping()" class="btn btn-success">Submit</button>
					</div>
				</div>
			</div>
		</div>
	</div>

</body>
</html>

