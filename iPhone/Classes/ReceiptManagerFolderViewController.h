//
//  ReceiptManagerFolderViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "UnifiedImagePicker.h"


@interface ReceiptManagerFolderViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate, 
UnifiedImagePickerDelegate>
{
	UIScrollView			*scroller;
	NSMutableDictionary		*images;
	NSMutableArray			*imagePos, *imageData;
	int						viewType, sortType, coverFlowImageIndex, viewTypeHold;
	BOOL					sortDirection, isPickerMode;
	UITableView				*tableView;
	MobileViewController	*parentMVC;
	NSString				*imageName;
	
	UILabel					*lblBack, *titleLabel; 
	UIImageView				*ivBack;

	UIToolbar				*fakeTB;
	UIView					*noDataView;
	UILabel					*noDataLabel;
}

@property (nonatomic, retain) UIToolbar *fakeTB;
@property (nonatomic, retain) IBOutlet UIScrollView *scroller;
@property (nonatomic, retain) NSMutableDictionary *images;
@property (nonatomic, retain) NSMutableArray *imagePos;
@property (nonatomic, retain) NSMutableArray *imageData;
@property (nonatomic, retain) UITableView *tableView;
@property (retain, nonatomic) IBOutlet UIView *noDataView;
@property int viewType;
@property int sortType; //0 = Alpha, 1=Date
@property BOOL sortDirection; //YES = ascending
@property BOOL isPickerMode;
@property (retain, nonatomic) MobileViewController	*parentMVC;

@property int coverFlowImageIndex;
@property int viewTypeHold;
@property (retain, nonatomic) NSString				*imageName;

@property (retain, nonatomic) UILabel					*lblBack;
@property (retain, nonatomic) UILabel					*titleLabel; 
@property (retain, nonatomic) UIImageView				*ivBack;
@property (retain, nonatomic) IBOutlet UILabel			*noDataLabel; 


- (IBAction)buttonClicked:(id)sender;
-(void)loadThumbs:(int)columns;
-(void)removeImageThumbs;
-(void)loadTableView;
- (IBAction)buttonCoverFlow:(id)sender;
-(void)buttonSortDirection:(id)sender;
-(void)buttonSortType:(id)sender;
-(void)buttonGridView:(id)sender;
-(void)loadTableViewButtons;
-(BOOL)isLandscape;
-(NSData *)getImageData:(NSString *) fileName;
-(void)buttonAddFromPhotoAlbumPressed:(id)sender;
-(void)loadGridViewButtons:(id)sender;
-(void) sortReceipts;

-(void)makePickerGridButtons;
-(void)makePickerTableButtons;

-(void)doConfigureToolBar:(id)sender;
@end
