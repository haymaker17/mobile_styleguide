//
//  ViewPanel.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Localizer.h"
#import "ExSystem.h" 
#import "RootViewController.h"

@interface ViewPanel : UIView {
	UIScrollView		*scroller;
	NSMutableArray		*aViews;
	NSMutableDictionary *dictViews;
	NSString			*panelTitle, *panelTag;
	NSString			*key;
}

@property (retain, nonatomic) UIScrollView			*scroller;
@property (retain, nonatomic) NSMutableArray		*aViews;
@property (retain, nonatomic) NSString				*panelTitle;
@property (retain, nonatomic) NSString				*panelTag;
@property (retain, nonatomic) NSString				*key;
@property (retain, nonatomic) NSMutableDictionary	*dictViews;

-(void) makeBasicPanel:(NSString *)pnlTitle pnlTag:(NSString *)pnlTag;
-(UIView *) makeAddButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target lblX:(float) lblX;
-(id)init;
-(UIView *) makeBigButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target;
-(BOOL)isLandscape;
-(UIView *) makeCardButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target;
-(UIView *) makeTripButton:(NSString *) btnTitle action:(NSString *)action imageName:(NSString *) imageName tagName:(NSString *)tagName x:(float) x y:(float) y target:(UIViewController *) target;
@end
