<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html ng-app="ieprofile">
<head>
<!--  <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular.js"></script>
 <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular-sanitize.js"></script>
 <script src="http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.4.0.js"></script>
 <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css" rel="stylesheet"> -->
 
 <!-- <script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.0.8/angular.js"></script>
    <script src="http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.6.0.js"></script>
    <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css" rel="stylesheet">
    -->
     <link data-require="bootstrap-css@*" data-semver="2.3.2" rel="stylesheet" href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css" />
    
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.6/angular.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/angular-strap/0.7.2/angular-strap.min.js"></script>
    <script src="script.js"></script>
 
<style type="text/css">
.nav, .pagination, .carousel, .panel-title a { cursor: pointer; }
</style>
<script type="text/javascript">
var msgIds = [];   

//var app = angular.module('ieprofile', ['ui.bootstrap']);

var app = angular.module('ieprofile', ['$strap.directives']);

	if(typeof(EventSource)!=="undefined") 
	{ 
		var source = new EventSource("/ieprofile/gmailListener");
	
		source.addEventListener('message', function(e) {
			  var objJSON = eval("(function(){return " + e.data + ";})()");
			  console.log(objJSON);
			   if(objJSON.newEmail && (msgIds.indexOf(objJSON.msgId) == -1)) {
				  console.log("will show alert");
				  msgIds.push(objJSON.msgId);
			  	  //angular.element(document.getElementById('newmail')).scope().addAlert(objJSON);
			  	var scope = angular.element(document.getElementById('newmail')).scope();
			  	scope.$apply(function() {
			  	    scope.successFn(objJSON);
			  	  });
			  	notifyMe(objJSON);
			  } 
			}, false);
	
			source.addEventListener('open', function(e) {
			  // Connection was opened.
			}, false);
	
			source.addEventListener('error', function(e) {
			  if (e.readyState == EventSource.CLOSED) {
			    // Connection was closed.
			  }
			}, false);
	} else { 
		alert("Sorry , your browser does not support server-sent events..."); 
	} 
	
	


	app.directive('notification', function($timeout){
	  return {
	    restrict: 'E',
	    replace: true,
	    scope: {
	      ngModel: '='
	    },
	    template: '<div class="alert" bs-alert="ngModel"></div>',
	    link: function(scope, element, attrs) {
	      scope.$watch('ngModel', function() {
	        element.show();
	        element.addClass('in');
	        $timeout(function(){
	          //element.empty();
	          element.hide();
	        }, 3000);
	      });
	    }
	  }
	});
	
	app.controller('AlertController', function($scope){
	    $scope.message = {
	      "type": "info",
	      "title":"Hey!",
	      "content": " Welcome back, new email alerts will come here."
	    };
	    
	    $scope.successFn = function(data) {
	    	var alertMsg = " From :"+ data.from +" Subject: "+data.subject;
	      $scope.message = {
	        "type": "success",
	        "title": "You have a new email..!",
	        "content": alertMsg
	      }
	      console.log('changing message to ' + $scope.message.type);
	    };
	});

/*  function AlertDemoCtrl($scope) {
	$scope.alerts = [];

	  $scope.addAlert = function(data) {
		  var alertMsg = " From :"+ data.from +" Subject: "+data.subject;
		  console.log(alertMsg);
	    $scope.alerts.push({ msg: alertMsg});
	      if (timeout) {
	        $timeout(function(){
	          $scope.closeAlert($scope.alerts.indexOf(alert));
	        }, timeout);
	      }  
	  };

	  $scope.closeAlert = function(index) {
	    $scope.alerts.splice(index, 1);
	  };
	}  */

	function notifyMe(data) {
		console.log("inn notify me");
		  // Let's check if the browser supports notifications
		  
		 /*  if (window.webkitNotifications.checkPermission() == 0) {
			  var gNotification = window.webkitNotifications.createNotification('icon.png', 'Item Saved', 'My Application Name');
			  gNotification.show();
		   } else {
			    window.webkitNotifications.requestPermission(function(){});
		   } */
		   console.log("inn notify me 2 "+Notification.permission);		  
		  if (!("Notification" in window)) {
		    alert("This browser does not support desktop notification");
		  }

		  // Let's check if the user is okay to get some notification
		  else if (Notification.permission === "granted") {
		    // If it's okay let's create a notification
		    console.log("inn notify me 1");
		    if(data != 'test') {
		    	var notification = new Notification('You have a new email..!', {body: " From :"+ data.from +"<br/> Subject: "+data.subject, icon: ".icon"});
		    }
		  }

		  // Otherwise, we need to ask the user for permission
		  // Note, Chrome does not implement the permission static property
		  // So we have to check for NOT 'denied' instead of 'default'
		  else if (Notification.permission !== 'denied') {
			  console.log("inn notify me 8 ");
		    Notification.requestPermission(function (permission) {

		      // Whatever the user answers, we make sure we store the information
		      if(!('permission' in Notification)) {
		        Notification.permission = permission;
		      }
		      console.log("inn notify me 3 "+permission);
		      // If the user is okay, let's create a notification
		      if (permission === "granted") {
		    	  if(data != 'test') {
		        	var notification = new Notification('You have a new email..!',{body: " From :"+ data.from +"<br/> Subject: "+data.subject, icon: ".icon"});
		    	  }
		      }
		    });
		  } 

		  // At last, if the user already denied any notification, and you 
		  // want to be respectful there is no need to bother him any more.
		}
	
</script>
</head>
<body>
<div ng-controller="AlertController" id="newmail">
 	<notification ng-model="message" ></notification>
    <!--   <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)">{{alert.msg}}</alert> -->
  <!--  <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.header}}</strong> <br/>{{alert.msg}}</alert>-->
</div>
<button onclick="notifyMe('test')">Allow Desktop notification..!!</button>
	<h1>Gmail</h1>
	
	<h2>WELCOME, <c:out value="${user.name}" escapeXml="true"/>  </h2> <!-- <img src="<c:out value='${user.picture}' escapeXml='true'/>"/>-->

	<h2>Inbox</h2>
	
	<c:forEach var="message" items="${messages}" >
		Subject : <c:out value="${message.subject}"/> <br>
		From : <c:out value="${message.from}"/> <br>
		to : <c:out value="${message.to}"/> <br>
		Content : <c:out value="${message.content}" escapeXml="true"/> <br>
	</c:forEach>


</body>
</html>