//
//  CCToastMsg.m
//  Toast
//
//  Created by laurent mery on 10/11/2014.
//
//



#import "CCToastMsg.h"
#import "UIColor+ConcurColor.h"
#import "UIView+Styles.h"


@implementation CCToastMsg {
	
	double widthPourcMax;
	CGFloat widthMini;
	CGFloat margeHzt, margeVrt, margeImgText, margeBetweenTwoToasts;
	CGFloat imgWidth, imgHeight;
	
	NSDictionary *images;
	NSMutableArray *toastActives;
	NSInteger maxToastActives;
    NSInteger duration;
	
	NSMutableDictionary *fluxToast;
	NSInteger fluxToastIndex;
}

//public
-(id)initWithView:(UIView*)view{
	
	if (self = [super init]){
		
		widthPourcMax = 0.53;
		widthMini = 100.0;
		margeHzt = 20.0;
		margeVrt = 10.0;
		margeImgText = 5.0;
		margeBetweenTwoToasts = 5.0;
		
		maxToastActives = 3;
        duration = 3;
		
		images =  @{
					  @"warning": [UIImage imageNamed:@"icontoastmsg-warning"]
					  };
		
		self.view = view;
		
		toastActives = [[NSMutableArray alloc] init];
		
		fluxToast = [[NSMutableDictionary alloc] init];
		fluxToastIndex = 0;
	}
	return self;
}

//public
- (void)toastWarningMessage:(NSString *)message {
	
	NSString *toastIndex = [self newToastIndex];
	[self toastMessage:message type:@"warning" forToastIndex:toastIndex];
	[self showToastAtToastIndex:toastIndex withDuration:duration];
}

-(NSString*)newToastIndex{
	
	fluxToastIndex++;
	
	NSMutableDictionary *dico = [[NSMutableDictionary alloc] init];
	
	NSString *toastIndex = [NSString stringWithFormat:@"%ld",(long)fluxToastIndex];
	[fluxToast setObject:dico forKey:toastIndex];
	
	return toastIndex;
}


-(void)showToastAtToastIndex:(NSString*)toastIndex withDuration:(NSInteger)secondes{
	
	NSDictionary *dico = [fluxToast objectForKey:toastIndex];

	UIView *toast = [dico objectForKey:@"viewContainer"];
	
	
	[self activesPushToastIndex:toastIndex];
	
	
	[self.view addSubview:toast];
	
	[self performSelector:@selector(endTimerForToastIndex:) withObject:toastIndex afterDelay:secondes];
}

-(void)activesPushToastIndex:(NSString*)toastIndex{
	
	NSDictionary *dico = [fluxToast objectForKey:toastIndex];
	UIView *toast = [dico objectForKey:@"viewContainer"];
	CGFloat currentHeight = toast.frame.size.height;
	
	if ([toastActives count] > 0){
		
		for (NSString *activetoastIndex in toastActives){
			
			currentHeight = [self shiftActivesToastIndex:activetoastIndex forHeight:currentHeight];
		}
	}
	
	[toastActives addObject:toastIndex];
	
	if ([toastActives count] > maxToastActives){
		
		NSString *oldIndex = [toastActives objectAtIndex:0];
		[self endTimerForToastIndex:oldIndex];
	}
	
}

-(CGFloat)shiftActivesToastIndex:(NSString*)toastIndex forHeight:(CGFloat)previousHeight{
	
	NSDictionary *dico = [fluxToast objectForKey:toastIndex];
	UIView *toast = [dico objectForKey:@"viewContainer"];
	
	CGFloat currentHeight = toast.frame.size.height;
	
	CGFloat delta = (previousHeight + margeBetweenTwoToasts);
	CGFloat toasty = toast.frame.origin.y - delta;
	[UIView animateWithDuration:0.25 animations:^{
		toast.frame =  CGRectMake(toast.frame.origin.x, toasty, toast.frame.size.width, toast.frame.size.height);
	}];
	
	
	
	return currentHeight;
}

-(void)endTimerForToastIndex:(NSString*)toastIndex{

	[[self class] cancelPreviousPerformRequestsWithTarget:self selector:@selector(endTimerForToastIndex:) object:toastIndex];
	
	NSDictionary *dico = [fluxToast objectForKey:toastIndex];
	
	UIView *toast = [dico objectForKey:@"viewContainer"];
	UIView *imageView = [dico objectForKey:@"imageView"];
	UIImageView *image = [dico objectForKey:@"image"];
	UILabel	*message = [dico objectForKey:@"message"];
	
	[image setHidden:YES];
	[message setHidden:YES];
	
	[imageView setHidden:YES];
	[imageView removeFromSuperview];
	
	CGFloat toastx = toast.frame.origin.x + (toast.frame.size.width / 2);
	CGFloat toasty = toast.frame.origin.y + (toast.frame.size.height / 2);
	[UIView animateWithDuration:0.25 animations:^{
		toast.frame =  CGRectMake(toastx, toasty, 0, 0);
	}];
		
	[toastActives removeObject:toastIndex];
	[fluxToast removeObjectForKey:toastIndex];
	
	image = nil;
	message = nil;
}

- (void)toastMessage:(NSString *)message type:(NSString *)type forToastIndex:(NSString*)toastIndex {
	
	
	UIImageView *image = [[UIImageView alloc] initWithImage:[images objectForKey:type]];
	imgWidth = image.frame.size.width;
	imgHeight = image.frame.size.height;
	
	double viewWidthMax = self.view.frame.size.width;
	double messageWidth = viewWidthMax * widthPourcMax;
	messageWidth = messageWidth < widthMini ? widthMini : messageWidth;

	// message
	
	UILabel *messageLabel = [[UILabel alloc] initWithFrame:CGRectMake(margeHzt, margeVrt + imgHeight + margeImgText, messageWidth, 26.0)];
	messageLabel.numberOfLines = 0; //multiline
	messageLabel.lineBreakMode = NSLineBreakByWordWrapping;
	[messageLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:16.0]];
	messageLabel.textColor = [UIColor whiteColor];
	messageLabel.backgroundColor = [UIColor clearColor];
	messageLabel.alpha = 1.0;
	messageLabel.text = message;
	[messageLabel setTextAlignment:NSTextAlignmentCenter];

	//TODO calcul height
	CGFloat messageHeight = [messageLabel getTextViewFitToContentWithHeightMax:500];
	messageLabel.frame = CGRectMake(margeHzt, margeVrt + imgHeight + margeImgText, messageWidth, messageHeight);

	
	
	CGFloat containerWidth = messageWidth + (2 * margeHzt);
	UIView *imageView = [[UIView alloc]initWithFrame:CGRectMake((containerWidth / 2) - (imgWidth / 2), margeVrt, imgWidth, imgHeight)];
	[imageView addSubview:image];
	
	
	//container
	
	UIView *viewContainer = [[UIView alloc] initWithFrame:CGRectMake(0.0, 0.0, containerWidth, messageHeight + (2 * margeVrt) + margeImgText + imgHeight)];
	viewContainer.layer.cornerRadius = 8.0;
	viewContainer.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
	
	[viewContainer addSubview:imageView];
	[viewContainer addSubview:messageLabel];
	
	viewContainer.center = self.view.center;

	NSDictionary *dico = [fluxToast objectForKey:toastIndex];
	
	[dico setValue:imageView forKey:@"imageView"];
	[dico setValue:image forKey:@"image"];
	[dico setValue:messageLabel forKey:@"message"];
	[dico setValue:viewContainer forKey:@"viewContainer"];
}


- (void)dealloc
{
	_view = nil;
	[[self class] cancelPreviousPerformRequestsWithTarget:self];
}

@end
