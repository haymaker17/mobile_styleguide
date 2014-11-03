//
//  ViewPanel.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ViewPanel.h"


@implementation ViewPanel
@synthesize	scroller;
@synthesize	aViews;
@synthesize	panelTitle, key, dictViews, panelTag;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
		self.aViews = [[NSMutableArray alloc] initWithObjects:nil];
		[aViews release];
		
		self.dictViews = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		[dictViews release];
    }
    return self;
}


-(id)init 
{
	[super init];
	self.aViews = [[NSMutableArray alloc] initWithObjects:nil];
	[aViews release];
	
	self.dictViews = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	[dictViews release];
	
	return self;
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code.
}
*/

- (void)dealloc {
	[dictViews release];
	[key release];
	[scroller release];
	[aViews release];
	[panelTitle release];
    [super dealloc];
}


-(void) makeBasicPanel:(NSString *)pnlTitle pnlTag:(NSString *)pnlTag
{
	float w = 300;
	//float h = 396;
	
	if([self isLandscape])
	{
		w = 460;
		//h = 320 - 84.0;
	}
	//ViewPanel *v = [[ViewPanel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	[self setBackgroundColor:[UIColor clearColor]];
	self.panelTag = pnlTag;
	self.panelTitle = pnlTitle;
	//self.tag = panelTitle;
	
	UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeRoundedRect];
	btnBack.frame = CGRectMake(0, 0, w, 392);
	btnBack.tag = 900;
	[self addSubview:btnBack];
	
	UILabel *lblTitle = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, 25)];
	lblTitle.text = [Localizer getLocalizedText:pnlTitle];
	[lblTitle setFont:[UIFont boldSystemFontOfSize:18]];
	[lblTitle setTextColor:[UIColor blackColor]];
	//[lblTitle setShadowColor:[UIColor grayColor]];
	//[lblTitle setShadowOffset:CGSizeMake(-1, 0)];
	[lblTitle setTextAlignment:UITextAlignmentCenter];
	[lblTitle setBackgroundColor:[UIColor clearColor]];
	[self addSubview:lblTitle];
	[lblTitle release];
	
	lblTitle = [[UILabel alloc] initWithFrame:CGRectMake(3, 27, w - 6, 1)];
	[lblTitle setBackgroundColor:[UIColor blackColor]];
	[lblTitle setAlpha:0.5];
	lblTitle.text = @"";
	[self addSubview:lblTitle];
	[lblTitle release];

	self.scroller = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 34, w, 350)];
	[self addSubview:scroller];
}


-(UIView *) makeAddButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target lblX:(float) lblX
{
	UIImage *img = [UIImage imageNamed:[ExSystem getImageNameForAll:imageName]];
	float w = img.size.width;
	float h = img.size.height;
	
	UIView * v = [[UIView alloc] initWithFrame:CGRectMake(x, y, w, h)];
	//v.tag = tagName;
	
//	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
//	iv.image = img;
//	[v addSubview:iv];
//	[iv release];
	
	UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
	btnBack.frame = CGRectMake(0, 0, w, h);
	[btnBack addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
	[btnBack setBackgroundImage:img forState:UIControlStateNormal];
	[v addSubview:btnBack];
	
	UILabel *lbl = nil;
	if(lblX > -1)
		lbl = [[UILabel alloc] initWithFrame:CGRectMake(lblX, 5, (w - lblX) - 5, h - 10)];
	else 
		lbl = [[UILabel alloc] initWithFrame:CGRectMake((w / 2), 5, (w / 2) - 5, h - 10)];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setFont:[UIFont systemFontOfSize:14]];
	lbl.text = [Localizer getLocalizedText:btnTitle];
	lbl.tag = 100;
	[lbl setNumberOfLines:3];
	[v addSubview:lbl];
	[lbl release];
	
//	UIButton *btnClick = [UIButton buttonWithType:UIButtonTypeCustom];
//	btnClick.frame = CGRectMake(0, 0, w, h);
//	[btnClick addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
//	[v addSubview:btnClick];
	
	[aViews addObject:v];
	[dictViews setObject:v forKey:tagName];
	
	return [v autorelease];
}

-(UIView *) makeBigButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target
{
	UIImage *img = [UIImage imageNamed:[ExSystem getImageNameForAll:imageName]];
	float w = img.size.width;
	float h = img.size.height;
	
	UIView * v = [[UIView alloc] initWithFrame:CGRectMake(x, y, w, h)];
	v.tag = [tagName integerValue];
//	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
//	iv.image = img;
//	[v addSubview:iv];
//	[iv release];
	
	UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
	btnBack.frame = CGRectMake(0, 0, w, h);
	[btnBack addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
	[btnBack setBackgroundImage:img forState:UIControlStateNormal];
	btnBack.tag = 900;
	[v addSubview:btnBack];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(5, (h / 3) * 2, (w  - 10), (h / 3))];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setFont:[UIFont systemFontOfSize:12]];
	lbl.text = [Localizer getLocalizedText:btnTitle];
	[lbl setTextAlignment:UITextAlignmentCenter];
	[lbl setNumberOfLines:3];
	lbl.tag = 100;
	[v addSubview:lbl];
	[lbl release];
	
//	UIButton *btnClick = [UIButton buttonWithType:UIButtonTypeCustom];
//	btnClick.frame = CGRectMake(0, 0, w, h);
//	[btnClick addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
//	[v addSubview:btnClick];
	
	[aViews addObject:v];
	[dictViews setObject:v forKey:tagName];
	
	return [v autorelease];
}


-(UIView *) makeCardButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target
{
	UIImage *img = [UIImage imageNamed:[ExSystem getImageNameForAll:imageName]];
	float w = 290;
	float h = 58;
	
	UIView * v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	iv.image = img;
	[v addSubview:iv];
	[iv release];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(85, 0, (w - 85), h)];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setFont:[UIFont systemFontOfSize:16]];
	lbl.text = btnTitle;
	[lbl setTextAlignment:UITextAlignmentLeft];
	[lbl setNumberOfLines:3];
	[lbl setTag:100];
	[v addSubview:lbl];
	[lbl release];
	
	UIButton *btnClick = [UIButton buttonWithType:UIButtonTypeCustom];
	btnClick.frame = CGRectMake(0, 0, w, h);
	[btnClick addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
	[v addSubview:btnClick];
	
	[aViews addObject:v];
	[dictViews setObject:v forKey:tagName];
	
	return [v autorelease];
}


-(UIView *) makeTripButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target
{
	UIImage *img = [UIImage imageNamed:[ExSystem getImageNameForAll:imageName]];
	float w = 290;
	float h = 58;
//	float x = 40;
//	float y = 0;
	
	UIView * v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	iv.image = img;
	[v addSubview:iv];
	[iv release];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(85, 10, (w - 85) - 3, 20)];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setFont:[UIFont boldSystemFontOfSize:16]];
	lbl.text = btnTitle;
	[lbl setTextAlignment:UITextAlignmentLeft];
	[lbl setLineBreakMode:UILineBreakModeMiddleTruncation];
	[lbl setNumberOfLines:1];
	[lbl setTag:100];
	[v addSubview:lbl];
	[lbl release];
	
	lbl = [[UILabel alloc] initWithFrame:CGRectMake(85, 35, (w - 85) - 3, 16)];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setFont:[UIFont systemFontOfSize:14]];
	lbl.text = btnTitle;
	[lbl setTextAlignment:UITextAlignmentLeft];
	[lbl setNumberOfLines:1];
	[lbl setTag:101];
	[v addSubview:lbl];
	[lbl release];
	
	UIButton *btnClick = [UIButton buttonWithType:UIButtonTypeCustom];
	btnClick.frame = CGRectMake(0, 0, w, h);
	[btnClick addTarget:target action:NSSelectorFromString(action)  forControlEvents:UIControlEventTouchUpInside];
	[v addSubview:btnClick];
	
	[aViews addObject:v];
	[dictViews setObject:v forKey:tagName];
	
	return [v autorelease];
}
	   
 -(BOOL)isLandscape
{
	UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
	if (orientation == UIDeviceOrientationLandscapeLeft  || orientation == UIDeviceOrientationLandscapeRight ) 
	{
		//[rootViewController setWasLandscape:YES];
		return YES;
	}
	else if (orientation == UIDeviceOrientationPortrait  || orientation == UIDeviceOrientationPortraitUpsideDown ) 
	{
		//[rootViewController setWasLandscape:NO];
		return NO;
	}
//	else if (orientation == UIDeviceOrientationUnknown||orientation == UIDeviceOrientationFaceDown||orientation == UIDeviceOrientationFaceUp) 
//	{
//		// If it's flat, just leave it alone
//		return [rootViewController wasLandscape];
//	}
	
	return NO;
}
@end
