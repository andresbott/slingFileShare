/*
By Osvaldas Valutis, www.osvaldas.info
Available for use under the MIT License
*/
'use strict';
;( function( $, window, document, undefined ){
$('.fsh-submit').prop('disabled', true);
$( '.fsh-input' ).each( function(){
    var $input	 = $( this ),
        $label	 = $input.next( 'label' ),
        labelVal = $label.html();

    $input.on( 'change', function( e ){
        var fileName = '';
        if( this.files && this.files.length > 1 )
            fileName = ( this.getAttribute( 'data-multiple-caption' ) || '' ).replace( '{count}', this.files.length );
        else if( e.target.value )
            fileName = e.target.value.split( '\\' ).pop();
        if( fileName )
            $label.find( ".fsh-input-text" ).html( fileName );
        else
            $label.html( labelVal );
        $('.fsh-submit').prop('disabled', false);
    });

    // Firefox bug fix
    $input
        .on( 'focus', function(){ $input.addClass( 'has-focus' ); })
        .on( 'blur', function(){ $input.removeClass( 'has-focus' ); });
});

})( jQuery, window, document );
