//
//  HomeButton.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HomeButton.h"


@implementation HomeButton

@synthesize imageName;
@synthesize labelText;
@synthesize buttonName;
@synthesize actionName;
@synthesize sectionName;
@synthesize row;
@synthesize col;
@synthesize landRow;
@synthesize landCol;
@synthesize homeView;
@synthesize x;
@synthesize y;
@synthesize imgHeight;
@synthesize imgWidth;
@synthesize badgeCount;
@synthesize product;
@synthesize btn;
@synthesize lbl;
@synthesize imgBadge;
@synthesize lblBadge;
@synthesize hpvc;


-(void)refreshBadge:(int)newCount
{
	if (newCount > 0) 
	{
		[lblBadge setHidden:NO];
		[imgBadge setHidden:NO];
	}
	else {
		[lblBadge setHidden:YES];
		[imgBadge setHidden:YES];
	}

	lblBadge.text = [NSString stringWithFormat:@"%d", newCount];
}

-(void)clearButton
{
	if(btn != nil)
		[btn removeFromSuperview];
	if(lbl != nil)
		[lbl removeFromSuperview];
	if(imgBadge != nil)
		[imgBadge removeFromSuperview];
	if(lblBadge != nil)
		[lblBadge removeFromSuperview];
}

-(void)reDrawButton:(int)xPos YPos:(int)yPos Width:(int)w Height:(int)h ViewToUse:(UIView *) viewToUse
{
//#ifdef BREEZE
	btn.frame = CGRectMake(xPos, yPos, imgWidth, imgHeight);	
	lbl.frame = CGRectMake(xPos -5, yPos + 66, imgWidth + 10, 15);
	imgBadge.frame = CGRectMake((xPos + imgWidth) - 20, yPos - 8, 29, 31);
	lblBadge.frame = CGRectMake((xPos + imgWidth) - 15, yPos -3, 18, 18);
//#else
//	btn.frame = CGRectMake(xPos, yPos, 60, 61);
//	lbl.frame = CGRectMake(xPos - 10, yPos + 55, 80, 44);
//	imgBadge.frame = CGRectMake(xPos + 43, yPos - 7, 29, 31);
//	lblBadge.frame = CGRectMake(xPos + 48, yPos - 3, 18, 18);
//#endif
}

-(void)buttonTestPressed:(id)sender
{
    [hpvc buttonTestPressed:self];
}


-(void)buttonActionPressed:(id)sender
{
	[hpvc buttonActionPressed:self];
}


-(void)buttonAmtrakPressed:(id)sender
{
	[hpvc buttonAmtrakPressed:self];
}

-(void)buttonAirportsPressed:(id)sender
{
	[hpvc buttonAirportsPressed:self];
}


-(void)buttonCardsPressed:(id)sender
{
	[hpvc buttonCardsPressed:self];
}

-(void)buttonApproveReportsPressed:(id)sender
{
	[hpvc buttonApproveReportsPressed:self];
}

-(void)buttonApproveInvoicesPressed:(id)sender
{
	[hpvc buttonApproveInvoicesPressed:self];
}

- (void)buttonCarPressed:(id)sender
{
	[hpvc buttonCarPressed:self];	
}

- (void)buttonHotelPressed:(id)sender
{
	[hpvc buttonHotelPressed:self];	
}

-(void)buttonReportsPressed:(id)sender
{
	[hpvc buttonReportsPressed:self];
}

- (void)buttonTripsPressed:(id)sender
{
	[hpvc buttonTripsPressed:self];	
}

- (void)buttonDiningPressed:(id)sender
{
	[hpvc buttonDiningPressed:self];
}

- (void)buttonTaxiPressed:(id)sender
{
	[hpvc buttonTaxiPressed:self];
}

- (void)buttonReceiptManagerPressed:(id)sender
{
	[hpvc buttonReceiptManagerPressed:self];
}

- (void)buttonOutOfPocketPressed:(id)sender
{
	////NSLog(@"1");
	[hpvc buttonOutOfPocketPressed:self];
	////NSLog(@"2");
}

- (void)buttonAttendeePressed:(id)sender
{
	[hpvc showBumpHelpAlert:sender];
}

-(void)drawButton:(int)xPos YPos:(int)yPos ViewToUse:(UIView *) viewToUse FadeIn:(BOOL)fadeIn
{
//#ifdef BREEZE
	//CGRect myImageRect = CGRectMake(xPos -10, yPos + 55, 80, 44);
//	UILabel *lbl = [[UILabel alloc] initWithFrame:myImageRect];
//	[lbl setAdjustsFontSizeToFitWidth:YES];
//	lbl.font = [UIFont systemFontOfSize:17];
//    //lbl.textColor = [UIColor colorWithRed:0.22 green:0.54 blue:0.41 alpha:1.0];
//    lbl.textAlignment = UITextAlignmentCenter;
//	lbl.text = labelText;
//	lbl.shadowColor = [UIColor whiteColor];
//	lbl.numberOfLines = 2;
//	lbl.lineBreakMode = UILineBreakModeWordWrap;
//	[viewToUse.view addSubview:lbl];
//	[lbl release];
	self.x = xPos;
	self.y = yPos;
	CGRect myImageRect = CGRectMake(xPos, yPos, imgWidth, imgHeight);
	btn = [[UIButton alloc] initWithFrame:myImageRect];
	UIImage *image = [UIImage imageNamed:imageName];
	[btn setBackgroundImage:image forState:UIControlStateNormal];
	if(actionName != nil)
	{
		//NSLog(@"actionName=%@" , actionName);
		[btn addTarget:self action:NSSelectorFromString(actionName) forControlEvents:UIControlEventTouchUpInside];
	}
	if (fadeIn == YES) {
		btn.alpha = 0;
		//btn.frame = CGRectMake(-200, yPos, imgWidth, imgHeight);
	}
	[viewToUse addSubview:btn];
	

    myImageRect = CGRectMake(xPos, yPos + 10, imgWidth, 15);
	lbl = [[UILabel alloc] initWithFrame:myImageRect];
	[lbl setAdjustsFontSizeToFitWidth:YES];
	lbl.font = [UIFont boldSystemFontOfSize:11];
	lbl.textColor = [UIColor whiteColor];
	lbl.textAlignment = UITextAlignmentCenter;
	lbl.text = labelText;
	lbl.backgroundColor = [UIColor clearColor];
	lbl.shadowColor = [UIColor blackColor];
	[viewToUse addSubview:lbl];

	
//	if (badgeCount > 0)
//	{
		myImageRect = CGRectMake((xPos + imgWidth) - 15, yPos + 8, 29, 31);
		imgBadge = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBadge setImage:[UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"SBBadgeBG" ofType:@"png"]]];//[UIImage imageNamed:@"SBBadgeBG.png"]];
		if (fadeIn == YES) {
			imgBadge.alpha = 0;
		}
		[viewToUse addSubview:imgBadge];
		
		myImageRect = CGRectMake((xPos + imgWidth) - 34, yPos + 13, 18, 18);
		lblBadge = [[UILabel alloc] initWithFrame:myImageRect];
		[lblBadge setAdjustsFontSizeToFitWidth:YES];
		lblBadge.font = [UIFont boldSystemFontOfSize:16];
		lblBadge.textColor = [UIColor whiteColor];
		lblBadge.textAlignment = UITextAlignmentCenter;
		lblBadge.text = [NSString stringWithFormat:@"%d", badgeCount];
		lblBadge.backgroundColor = [UIColor clearColor];
		lblBadge.shadowOffset = CGSizeMake(0, -1);
		lblBadge.shadowColor = [UIColor blackColor];
		[viewToUse addSubview:lblBadge];
		
	
	if (badgeCount <= 0)
	{
		[lblBadge setHidden:YES];
		[imgBadge setHidden:YES];
	}
//	}
	//[btn release];
//#else
//
//	CGRect myImageRect = CGRectMake(xPos - 10, yPos + 55, 80, 44);
//	lbl = [[UILabel alloc] initWithFrame:myImageRect];
//	[lbl setAdjustsFontSizeToFitWidth:YES];
//	lbl.font = [UIFont systemFontOfSize:17];
//    //lbl.textColor = [UIColor colorWithRed:0.22 green:0.54 blue:0.41 alpha:1.0];
//    lbl.textAlignment = UITextAlignmentCenter;
//	lbl.text = labelText;
//	lbl.shadowColor = [UIColor whiteColor];
//	lbl.numberOfLines = 2;
//	lbl.backgroundColor = [UIColor clearColor];
//	lbl.lineBreakMode = UILineBreakModeWordWrap;
//	if (fadeIn == YES) {
//		lbl.alpha = 0;
//	}
//	[viewToUse addSubview:lbl];
//	//[lbl release];
//	
//	myImageRect = CGRectMake(xPos, yPos, 60, 61);
//	btn = [[UIButton alloc] initWithFrame:myImageRect];
//	UIImage *image = [UIImage imageNamed:imageName];
//	[btn setBackgroundImage:image forState:UIControlStateNormal];
//	//[btn addTarget:viewToUse action:NSSelectorFromString(actionName) forControlEvents:UIControlEventTouchUpInside];
//	if(actionName != nil)
//	{
//		//NSLog(@"actionName=%@" , actionName);
//		[btn addTarget:self action:NSSelectorFromString(actionName) forControlEvents:UIControlEventTouchUpInside];
//	}
//	if (fadeIn == YES) {
//		btn.alpha = 0;
//	}
////	btn.alpha = 0;
////	[UIView beginAnimations:@"Fade" context:nil];
////	[UIView setAnimationDelegate:self];
////	[UIView setAnimationDidStopSelector:@selector(animationDidStop: finished: context:)];
////	[UIView setAnimationDuration:1];
////	[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//
//	
//	[viewToUse addSubview:btn];
//	
//	if (badgeCount > 0)
//	{
//		myImageRect = CGRectMake(xPos + 43, yPos - 7, 29, 31);
//		imgBadge = [[UIImageView alloc] initWithFrame:myImageRect];
//		[imgBadge setImage:[UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"SBBadgeBG" ofType:@"png"]]];//[UIImage imageNamed:@"SBBadgeBG.png"]];
//		if (fadeIn == YES) {
//			imgBadge.alpha = 0;
//		}
//		[viewToUse addSubview:imgBadge];
//		
//		myImageRect = CGRectMake(xPos + 48, yPos - 3, 18, 18);
//		lblBadge = [[UILabel alloc] initWithFrame:myImageRect];
//		[lblBadge setAdjustsFontSizeToFitWidth:YES];
//		lblBadge.font = [UIFont boldSystemFontOfSize:16];
//		lblBadge.textColor = [UIColor whiteColor];
//		lblBadge.textAlignment = UITextAlignmentCenter;
//		lblBadge.text = [NSString stringWithFormat:@"%d", badgeCount];
//		lblBadge.backgroundColor = [UIColor clearColor];
//		lblBadge.shadowColor = [UIColor whiteColor];
//		[viewToUse addSubview:lblBadge];
//		//[lbl release];
//	}
////	btn.alpha = 1;
////	[UIView commitAnimations];
//	//[btn release];
//#endif

}

//delegate function:

-(void) animationDidStop:(NSString *)animationID finished:(NSNumber *)finished context:(void *)context
{
	//... invalidate button so it cannot be pressed ...
}

-(void) addToView
{
//	CGRect dragRect = CGRectMake(0.0f, 0.0f, 64.0f, 64.0f);
//	dragRect.origin = randomPoint();
//	DragView *dragger = [[DragView alloc] initWithFrame:dragRect];
//	NSString *whichFlower = [[NSArray arrayWithObjects:@"blueFlower.png", @"pinkFlower.png", @"orangeFlower.png", nil] objectAtIndex:(random() % 3)];
//	[dragger setImage:[UIImage imageNamed:whichFlower]];
	[self setUserInteractionEnabled:YES];
	[homeView.view addSubview:self];
//	[dragger release];
}

//drag and drop of the button
- (void) touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event
{
	// Retrieve the touch point
	CGPoint pt = [[touches anyObject] locationInView:self];
	startLocation = pt;
	[[self superview] bringSubviewToFront:self];
}

- (void) touchesMoved:(NSSet*)touches withEvent:(UIEvent*)event
{
	// Move relative to the original touch point
	CGPoint pt = [[touches anyObject] locationInView:self];
	CGRect frame = [self frame];
	frame.origin.x += pt.x - startLocation.x;
	frame.origin.y += pt.y - startLocation.y;
	[self setFrame:frame];
}

-(void)dealloc
{
	[imageName release];
	[labelText release];
	[buttonName release];
	[actionName release];
	[sectionName release];
	[row release];
//	[col release];
	[landRow release];
	[landCol release];
	[homeView release];
//	[x release];
//	[y release];
//	[imgHeight release];
//	[imgWidth release];
	[btn release];
	[lbl release];
	[imgBadge release];
	[lblBadge release];
	[hpvc release];
	[super dealloc];
}

@end
