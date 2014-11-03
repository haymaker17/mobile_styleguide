//
//  ReportRejectionViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ReportRejectionDelegate.h"
#import "ExSystem.h" 


@interface ReportRejectionViewController : MobileViewController
{
	UINavigationBar				*tBar;
	UITextView					*commentTextView;
	CGRect						keyboardBounds;
	id<ReportRejectionDelegate>	reportRejectionDelegate;
	
	UIBarButtonItem				*cancelBtn;
	UIBarButtonItem				*sendBackBtn;
}
// Mob-2517,2518
@property (nonatomic, strong) IBOutlet UINavigationBar		*tBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*cancelBtn;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*sendBackBtn;

@property (nonatomic, strong) IBOutlet UITextView			*commentTextView;
@property (nonatomic) CGRect								keyboardBounds;
@property (nonatomic, strong) id<ReportRejectionDelegate>	reportRejectionDelegate;

-(IBAction) cancelPressed:(id)sender;

-(IBAction) sendBackPressed:(id)sender;

-(void)keyboardDidShowNotification:(NSNotification*)notification;
-(void)keyboardWillHideNotification:(NSNotification*)notification;

-(void)adjustSizeOfCommentTextViewWithKeyboard;
-(void)adjustSizeOfCommentTextViewWithoutKeyboard;

@end
