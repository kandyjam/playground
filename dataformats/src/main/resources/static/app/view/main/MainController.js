Ext.define('DF.view.main.MainController', {
	extend: 'Ext.app.ViewController',
		
	onBeforeTabChange: function( tabPanel, newCard) {
		newCard.getStore().load();
	}
});