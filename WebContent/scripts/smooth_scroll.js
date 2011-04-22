/* Smooth scrolling
   Changes links that link to other parts of this page to scroll
   smoothly to those links rather than jump to them directly, which
   can be a little disorienting.

   sil, http://www.kryogenix.org/

   v1.0 2003-11-11
   v1.1 2005-06-16 wrap it up in an object
   
   05/18/2007 - Modified and adapted by Heston Fernandes
*/

	var ss = {
	  smoothScroll: function(anchor) {
	    // Now loop all A tags until we find one with that name
	    var allLinks = document.getElementsByTagName('a');
	    var destinationLink = null;
	    for (var i=0;i<allLinks.length;i++) {
	      var lnk = allLinks[i];
	      if (lnk.name && (lnk.name == anchor)) {
	        destinationLink = lnk;
	        break;
	      }
	    }
	
	    // If we didn't find a destination, give up and let the browser do
	    // its thing
	    if (!destinationLink) return true;
	
	    // Find the destination's position
	    var destx = destinationLink.offsetLeft; 
	    var desty = destinationLink.offsetTop;
	    var thisNode = destinationLink;
	    while (thisNode.offsetParent && 
	          (thisNode.offsetParent != document.body)) {
	      thisNode = thisNode.offsetParent;
	      destx += thisNode.offsetLeft;
	      desty += thisNode.offsetTop;
	    }
	
	    // Stop any current scrolling
	    clearInterval(ss.INTERVAL);
	
	    cypos = ss.getCurrentYPos();
	
	    ss_stepsize = parseInt((desty-cypos)/ss.STEPS);
	    ss.INTERVAL = setInterval('ss.scrollWindow('+ss_stepsize+','+desty+',"'+anchor+'")',10);
	
	  },
	  
	  scrollWindow: function(scramount,dest,anchor) {
	    wascypos = ss.getCurrentYPos();
	    isAbove = (wascypos < dest);
	    window.scrollTo(0,wascypos + scramount);
	    iscypos = ss.getCurrentYPos();
	    isAboveNow = (iscypos < dest);
	    if ((isAbove != isAboveNow) || (wascypos == iscypos)) {
	      // if we've just scrolled past the destination, or
	      // we haven't moved from the last scroll (i.e., we're at the
	      // bottom of the page) then scroll exactly to the link
	      window.scrollTo(0,dest);
	      // cancel the repeating timer
	      clearInterval(ss.INTERVAL);
	      // and jump to the link directly so the URL's right
	      location.hash = anchor;
	    }
	  },
	
	  getCurrentYPos: function() {
	    if (document.body && document.body.scrollTop)
	      return document.body.scrollTop;
	    if (document.documentElement && document.documentElement.scrollTop)
	      return document.documentElement.scrollTop;
	    if (window.pageYOffset)
	      return window.pageYOffset;
	    return 0;
	  }
	};
	
