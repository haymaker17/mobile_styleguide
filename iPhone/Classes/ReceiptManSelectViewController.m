    //
//  ReceiptManSelectViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptManSelectViewController.h"
#import "RootViewController.h"
#import "ImageUtil.h"

#define kCELL_HEIGHT 70

@implementation ReceiptManSelectViewController
@synthesize tableView, tableData, sectionData;


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
	return YES;
}


-(NSString *)getViewIDKey
{
	return RECEIPT_MANAGER_SELECTOR;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [super viewDidLoad];
	
	[rootViewController.navigationController.toolbar setHidden:NO];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnRefresh = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(buttonLogoutPressed:)];
	UIBarButtonItem *btnCamera = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera target:self action:@selector(buttonLogoutPressed:)];
	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonLogoutPressed:)];
	
	NSArray *items = [NSArray arrayWithObjects: btnRefresh, flexibleSpace, btnCamera, flexibleSpace, btnAdd,nil];
	[self setToolbarItems:items animated:YES];
	
	[flexibleSpace release];
	[btnRefresh release];
	[btnCamera release];
	[btnAdd release];
		
	CGRect tableCGRect =  CGRectMake(0, 0, 320, 400);
	tableView = [[UITableView alloc] initWithFrame:tableCGRect style:UITableViewStylePlain];
	[tableView setDelegate:self];
	[tableView setDataSource:self];
	NSString *photoAlbum = [Localizer getLocalizedText:@"Add from Photo Album"];
	NSString *receiptFolder = [Localizer getLocalizedText:@"Manage Receipts"];
	NSString *addEntry = [Localizer getLocalizedText:@"Add an Entry from a Picture"];

	sectionData = [[NSMutableArray alloc] initWithObjects:photoAlbum, receiptFolder, addEntry, nil];
	
	tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;	
	tableView.autoresizesSubviews = YES;
	tableView.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);

	[self.view addSubview: tableView];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];    
    // Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc 
{
	[tableView release];
	[tableData release];
	[sectionData release];
    [super dealloc];
}


#pragma mark -
#pragma mark UnifiedImagePickerDelegate methods
-(void)unfiedImagePickerSelectedImage:(UIImage*)image
{
	[[[UnifiedImagePicker sharedInstance] imagePicker] dismissModalViewControllerAnimated:YES];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:image, @"IMAGE", nil];
	[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_SELECTOR ParameterBag:pBag];
	[pBag release];
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [sectionData count];
}


- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	NSString *currSection = [sectionData objectAtIndex:row];
	
	UITableViewCell *cell = [tblView dequeueReusableCellWithIdentifier:@"BreezeData"];
	if (cell == nil) 
	{
		cell = [[[UITableViewCell alloc] initWithFrame:CGRectZero reuseIdentifier:@"BreezeData"] autorelease];
	}
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(73, 0, 240, kCELL_HEIGHT)];
	lbl.font = [UIFont boldSystemFontOfSize:20];
	lbl.text = currSection;
	lbl.lineBreakMode = UILineBreakModeWordWrap;
	lbl.numberOfLines = 2;
	[cell.contentView addSubview:lbl];
	[lbl setTextColor:[UIColor grayColor]];
	[lbl setHighlightedTextColor:[UIColor whiteColor]];
	[lbl release];
	
	CGRect myImageRect = CGRectMake(2, 2, 66, 66);
	UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
	if ([currSection isEqualToString:@"Add from Photo Album"])
		[imgBack setImage:[UIImage imageNamed:@"ReceiptManager_photoalbum.png"]];
	else if ([currSection isEqualToString:@"Add from Camera Roll"])
		[imgBack setImage:[UIImage imageNamed:@"ReceiptManager_cameraroll.png"]];
	else if ([currSection isEqualToString:@"Manage Receipts"])
		[imgBack setImage:[UIImage imageNamed:@"ReceiptManager.png"]];
	else if ([currSection isEqualToString:@"Add an Entry from a Picture"])
		[imgBack setImage:[UIImage imageNamed:@"ReceiptManager_camera.png"]];
	[cell.contentView addSubview:imgBack];
	[imgBack release];
	
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	return cell;
}


#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSUInteger row = [newIndexPath row];
	if(row == 0)
	{
		UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
		imgPicker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
		[UnifiedImagePicker sharedInstance].delegate = self;
		[self presentModalViewController:imgPicker animated:YES];
	}
	else if(row == 1)
	{
		[rootViewController switchToView:RECEIPT_MANAGER_IMAGES viewFrom:RECEIPT_MANAGER_SELECTOR ParameterBag:nil];
	}
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return kCELL_HEIGHT;	
}

@end

