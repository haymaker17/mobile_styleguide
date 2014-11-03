//
//  RootViewController_iPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExSystem.h" 
#import "RootViewController.h"

@protocol SubstitutableDetailViewController
- (void)showRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem;
- (void)invalidateRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem;
@end



@class DetailViewController;


@interface RootViewController_iPad : UITableViewController <SubstitutableDetailViewController> {

	UISplitViewController		*splitViewController;
	UIPopoverController			*popoverController; 

    UIBarButtonItem				*rootPopoverButtonItem;
	
	DetailViewController		*detailViewController;
	RootViewController			*rvc;
	NSMutableArray				*menuArray;
	UINavigationBar				*navigationBar;
}

@property (nonatomic, retain) IBOutlet DetailViewController		*detailViewController;
@property (nonatomic, retain) RootViewController				*rvc;
@property (nonatomic, retain) NSMutableArray					*menuArray;


@property (nonatomic, assign) IBOutlet UISplitViewController	*splitViewController;

@property (nonatomic, retain) UIPopoverController				*popoverController;

@property (nonatomic, retain) UIBarButtonItem					*rootPopoverButtonItem;

@property (nonatomic, retain) IBOutlet UINavigationBar *navigationBar;

- (void)viewDidLoad;

-(void)resetButtons;
-(void)switchToDetail:(NSString *)menuItem ParameterBag:(NSMutableDictionary *)pBag;
-(void) loadMainMenu;
@end
