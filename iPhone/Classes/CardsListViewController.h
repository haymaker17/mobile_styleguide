//
//  CardsListViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"


@interface CardsListViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource> 
{
	UITableView				*tableView;
	NSMutableArray			*aKeys, *sections;
	NSMutableDictionary		*selectedRows;
	NSMutableDictionary		*dict;
	UILabel					*lblBack, *titleLabel; 
	UIImageView				*ivBack;
	BOOL					showedNo, drewEdit;
	BOOL selected;
	UIView					*waitView;
	

}

@property (retain, nonatomic) IBOutlet UITableView		*tableView;
@property (retain, nonatomic) NSMutableArray			*aKeys;
@property (retain, nonatomic) NSMutableDictionary		*selectedRows;
@property (retain, nonatomic) NSMutableDictionary		*dict;
@property (retain, nonatomic) UILabel					*lblBack;
@property (retain, nonatomic) UILabel					*titleLabel; 
@property (retain, nonatomic) UIImageView				*ivBack;
@property BOOL showedNo;
@property BOOL drewEdit;
@property (retain, nonatomic) UIView					*waitView;

@property (retain, nonatomic) NSMutableArray			*sections;

-(void)buttonCancelPressed:(id)sender;
-(void)buttonEditPressed:(id)sender;
-(void)buttonAddPressed:(id)sender;
- (void)clearSelectionForTableView:(UITableView *)tableView indexPath:(NSIndexPath *)indexPath;
- (BOOL)selected;

-(void)buttonAddToReportPressed:(id)sender;
-(void)buttonDeleteSelectedPressed:(id)sender;

-(void) makeBtnEdit;

@end
