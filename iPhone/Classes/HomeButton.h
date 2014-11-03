//
//  HomeButton.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExSystem.h" 
#import "RootViewController.h";


@interface HomeButton : UIView 
{
	CGPoint			startLocation;
	NSString		*imageName, *labelText, *buttonName, *actionName, *sectionName;
	NSNumber		*row, *landRow, *landCol;
	int				col, badgeCount, product, x, y, imgHeight, imgWidth;
	UIViewController *homeView;
	UIButton		*btn;
	UILabel			*lbl, *lblBadge;
	UIImageView		*imgBadge;
	RootViewController	*hpvc;
}

@property (nonatomic, retain) NSString *imageName;
@property (nonatomic, retain) NSString *labelText;
@property (nonatomic, retain) NSString *buttonName;
@property (nonatomic, retain) NSString *actionName;
@property (nonatomic, retain) NSString *sectionName;
@property (nonatomic, retain) NSNumber *row;
@property (nonatomic) int col;
@property (nonatomic) int badgeCount;
@property (nonatomic) int product;
@property (nonatomic, retain) NSNumber *landRow;
@property (nonatomic, retain) NSNumber *landCol;
@property (nonatomic) int x;
@property (nonatomic) int y;
@property (nonatomic) int imgHeight;
@property (nonatomic) int imgWidth;
@property (nonatomic, retain) UIViewController *homeView;
@property (nonatomic, retain) UIButton *btn;
@property (nonatomic, retain) UILabel *lbl;
@property (nonatomic, retain) UILabel *lblBadge;
@property (nonatomic, retain) UIImageView *imgBadge;
@property (nonatomic, retain) RootViewController	*hpvc;

-(void)drawButton:(int)xPos YPos:(int)yPos ViewToUse:(UIView *) viewToUse FadeIn:(BOOL)fadeIn;
-(void)reDrawButton:(int)xPos YPos:(int)yPos Width:(int)w Height:(int)h ViewToUse:(UIView *) viewToUse;

-(void)buttonApproveReportsPressed:(id)sender;
-(void)buttonReportsPressed:(id)sender;
-(void)buttonTripsPressed:(id)sender;
- (void)buttonDiningPressed:(id)sender;
- (void)buttonCarPressed:(id)sender;
- (void)buttonHotelPressed:(id)sender;
- (void)buttonTaxiPressed:(id)sender;
- (void)buttonOutOfPocketPressed:(id)sender;
-(void)buttonCardsPressed:(id)sender;
-(void)clearButton;
-(void)refreshBadge:(int)newCount;

-(void)buttonAmtrakPressed:(id)sender;
-(void)buttonAirportsPressed:(id)sender;
-(void)buttonActionPressed:(id)sender;
@end
