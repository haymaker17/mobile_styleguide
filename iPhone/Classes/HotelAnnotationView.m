//
//  HotelAnnotationView.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelAnnotationView.h"
#import "HotelMapViewController.h"


@implementation HotelAnnotationView


@synthesize mapController;
@synthesize hotelIndex;


-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
	if (mapController != nil)
		[mapController annotationSelected:self];
}

/*
- (id)initWithFrame:(CGRect)frame {
    if ((self = [super initWithFrame:frame])) {
        // Initialization code
    }
    return self;
}
*/
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/



@end
