//
//  ModalOptionsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ModalOptionsDelegate.h"

@interface ModalOptionsViewController :  MobileViewController <UITableViewDelegate, UITableViewDataSource>
{
	id<ModalOptionsDelegate>	__weak _delegate;

	UITableView					*tblView;
	UINavigationBar				*tBar;
	UIBarButtonItem				*cancelBtn;

	NSString					*optionTitle;
	NSArray						*labels;
	int							selectedRowIndex;
	CGFloat						preferredFontSize;
}

@property (nonatomic, weak) id<ModalOptionsDelegate>	delegate;

@property (nonatomic, strong) IBOutlet UITableView			*tblView;
@property (nonatomic, strong) IBOutlet UINavigationBar		*tBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem		*cancelBtn;

@property (nonatomic, strong) NSString						*optionTitle;
@property (nonatomic, strong) NSArray						*labels;
@property (nonatomic) int									selectedRowIndex;
@property (nonatomic) CGFloat								preferredFontSize;

-(IBAction) btnCancel:(id)sender;

-(void)updateTitle;
-(void)scrollToSelectedRow;

@end
