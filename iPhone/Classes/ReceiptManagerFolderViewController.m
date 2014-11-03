    //
//  ReceiptManagerFolderViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptManagerFolderViewController.h"
#import "RootViewController.h"
#import "FlowCoverViewController.h"
#import "FormatUtils.h"
#import "ImageUtil.h"
#import "MCLogging.h"

@implementation ReceiptManagerFolderViewController


#define kSORT_TYPE_NAME 0
#define kSORT_TYPE_DATE 1
#define kSORT_DIRECTION_ASC 0
#define kSORT_DIRECTION_DESC 1
#define kVIEW_TYPE_TABLE 1
#define kVIEW_TYPE_GRID 0
#define kVIEW_TYPE_CFLOW 2
#define kTHUMB_SIZE 75
#define kTHUMB_GAPPED (kTHUMB_SIZE + 4)


@synthesize scroller, images, imagePos, viewType, tableView, imageData, sortType, sortDirection, coverFlowImageIndex, viewTypeHold;
@synthesize isPickerMode, parentMVC, imageName;
@synthesize ivBack, lblBack, titleLabel, fakeTB;
@synthesize noDataView, noDataLabel;

#pragma mark -
#pragma mark MobileViewController Methods
//Overridden method to tell the RVC what this view is for
-(NSString *)getViewIDKey
{
	return RECEIPT_MANAGER_IMAGES;
}


//Overridden to tell RVC how you get here
-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

#pragma mark -
#pragma mark UnifiedImagePicker Delegate methods
-(void)unfiedImagePickerSelectedImage:(UIImage*)image
{
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
	[imgPicker dismissModalViewControllerAnimated:YES];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:image, @"IMAGE", nil];
	[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_SELECTOR ParameterBag:pBag];
	[pBag release];
}


#pragma mark -
#pragma mark View Controller Methods
- (void)viewWillAppear:(BOOL)animated
{
	[self configureToolBar];
}

//very concerned with coming from coverflow and rotation of device
- (void)viewDidAppear:(BOOL)animated 
{
	if([rootViewController.receiptData.receipts count] > 0)
	{
//		NSLog(@"1 emptyView Address %d", &emptyView);
//		NSLog(@"1 emptyView retain count %d", [emptyView retainCount]);
		[noDataView setHidden:YES];
//		[emptyView setHidden:YES];
//		[emptyView removeFromSuperview];
//		[emptyView release];
		//emptyView = nil;
	}
	else 
	{	
		//[self makeNoDataView:[Localizer getLocalizedText:@"No Receipts"]];
		//if (emptyView == nil) 
			//[self makeNoDataView:[Localizer getLocalizedText:@"No Receipts"]];
//		NSLog(@"2 emptyView Address %d", &emptyView);
//		NSLog(@"2 emptyView retain count %d", [emptyView retainCount]);
//		[emptyView setHidden:NO];
		[noDataLabel setText:[Localizer getLocalizedText:@"No Receipts"]];
		[noDataView setHidden:NO];
	}
	
	///[rootViewController.views removeObjectForKey:RECEIPT_MANAGER];
	[super viewDidAppear:animated];
	if (viewType == kVIEW_TYPE_GRID)
	{	
		[self removeImageThumbs];
		if ([self isLandscape]) 
			[self loadThumbs:5];
		else
			[self loadThumbs:3];
	}
	else if (viewType == kVIEW_TYPE_TABLE)
	{
		[self buttonTableView:self];
	}
	else if(viewType == kVIEW_TYPE_CFLOW)
	{
		viewType = viewTypeHold;
		if (coverFlowImageIndex > -1) 
		{
			ReceiptImageMetaData *rimd = [imagePos objectAtIndex:coverFlowImageIndex]; // objectAtIndex:coverFlowImageIndex];
			
			if(isPickerMode & rimd != nil)
			{
				self.imageName = rimd.imageName;
				[self performSelector:@selector(buttonSelectModal:) withObject:nil afterDelay:0.2f];
				//[self buttonSelectModal:self];
			}
			else 
			{
				NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rimd, @"IMAGE_DICTIONARY", nil];
				[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_IMAGES ParameterBag:pBag];
				[pBag release];
			}
		}
		
	}
	
	

	
	//[self makeNoDataView:[Localizer getLocalizedText:@"No Receipts"]];
	if(isPickerMode)
		[self.view bringSubviewToFront:fakeTB];
	
	//NSLog(@"Receipt Manager viewDidAppear");
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	sortType = kSORT_TYPE_DATE;
	sortDirection = kSORT_DIRECTION_ASC;
	imageData = [[NSMutableArray alloc] init];
	//scroller.delegate = self;

	//[self makeNoDataView:[Localizer getLocalizedText:@"No Receipts"]];
	//[self makeNoDataView:nil];
	
	if ([self isLandscape]) 
		[self loadThumbs:5];
	else
		[self loadThumbs:3];
	
	viewType = kVIEW_TYPE_GRID; //0 is the image grid view
	[super viewDidLoad];
	
	for (UIImageView *iView in [self.view subviews]) 
	{
		if (iView.tag >= 900) 
			[iView removeFromSuperview];
	}
	
	CGRect tableCGRect =  CGRectMake(0, 0, 320, 400);
	tableView = [[UITableView alloc] initWithFrame:tableCGRect style:UITableViewStylePlain];
	[tableView setDelegate:self];
	[tableView setDataSource:self];
	tableView.tag = 900;
	//	NSString *photoAlbum = @"Add from Photo Album";
	//	NSString *cameraRoll = @"Add from Camera Roll";
	//	NSString *receiptFolder = @"Manage Receipts";
	//	NSString *addEntry = @"Add an Entry from a Picture";
	//	
	//	sectionData = [[NSMutableArray alloc] initWithObjects:photoAlbum, cameraRoll, receiptFolder, addEntry, nil];
	//	
	tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
	tableView.autoresizesSubviews = YES;
	tableView.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
	//tableView.backgroundColor = [UIColor clearColor];
	//[tableView reloadData];	
	[self.view addSubview: tableView];
	[tableView setHidden:YES];
	
	
	//rootViewController.navigationController.toolbar.alpha = 0.25;
	
    //[super viewDidLoad];
	
	if(isPickerMode)
	{
		[self makePickerGridButtons];

		scroller.frame = CGRectMake(scroller.frame.origin.x, 44, 
									  scroller.frame.size.width, scroller.frame.size.height - 44); //resize
	}
	else 
		[self setCurrentViewValues];
}


-(void)configureToolBar
{
	[self performSelector:@selector(doConfigureToolBar:) withObject:nil afterDelay:0.05f];
}

-(void)doConfigureToolBar:(id)sender
{
	[rootViewController.navigationController.toolbar setHidden:NO];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnTable = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_table.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonTableView:)];
	UIBarButtonItem *btnCoverFlow = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonCoverFlow:)];
	//UIBarButtonItem *btnAlpha = [[UIBarButtonItem alloc] initWithTitle:@"A to Z" style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSortType:)];
	//UIBarButtonItem *btnDate = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_up.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	UIBarButtonItem *btnCamera = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera target:self action:@selector(buttonCameraPressed:)];
	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddFromPhotoAlbumPressed:)];
	UIBarButtonItem *btnAlpha;
	if(sortType == kSORT_TYPE_DATE)
	{
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_alphasort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)];
	}
	else {
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_datesort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)]; 
	}
	
	UIBarButtonItem *btnDir;
	if(sortDirection == kSORT_DIRECTION_ASC)
	{
		btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_down.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}
	else {
		btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_up.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}
	
	NSArray *items = [NSArray arrayWithObjects: btnTable, btnCoverFlow, flexibleSpace, btnAlpha, btnDir, flexibleSpace, btnAdd, flexibleSpace, btnCamera,  nil];
	
	[self setToolbarItems:items animated:YES];
	
	[flexibleSpace release];
	[btnTable release];
	[btnCoverFlow release];
	[btnAdd release];
	[btnCamera release];
	[btnAlpha release];
	[btnDir release];
}


-(void)makePickerGridButtons
{
	if(fakeTB == nil)
	{
		self.fakeTB = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, 44)];
		fakeTB.autoresizingMask = UIViewAutoresizingFlexibleWidth;
		fakeTB.tintColor = [RootViewController getBaseColor];
		[self.view addSubview:fakeTB];
	}
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
														  style:UIBarButtonSystemItemCancel 
														  target:self 
														  action:@selector(buttonCancelModal:)];	
	UIBarButtonItem *btnRM= [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt Folder"] style:UIBarButtonItemStylePlain target:self action:nil];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnTable = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_table.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonTableView:)];
	UIBarButtonItem *btnCoverFlow = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonCoverFlow:)];
	
	NSArray *toolbarItems = [NSArray arrayWithObjects: btnCancel, flexibleSpace, btnRM, flexibleSpace, btnTable, btnCoverFlow,  nil];
	[fakeTB setItems:toolbarItems animated:YES];
	[self.view addSubview:fakeTB];
	[btnCancel release];
	[btnRM release];
	[flexibleSpace release];
	[btnTable release];
	[btnCoverFlow release];
}


-(void)makePickerTableButtons
{
	if(fakeTB == nil)
	{
		self.fakeTB = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
		fakeTB.tintColor = [RootViewController getBaseColor];
		[self.view addSubview:fakeTB];
	}
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
																  style:UIBarButtonSystemItemCancel 
																 target:self 
																 action:@selector(buttonCancelModal:)];
	UIBarButtonItem *btnRM = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt Folder"] style:UIBarButtonItemStylePlain target:self action:nil];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnGrid = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_grid.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonGridView:)];
	UIBarButtonItem *btnCoverFlow = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonCoverFlow:)];
	
	NSArray *toolbarItems = [NSArray arrayWithObjects: btnCancel, btnRM,flexibleSpace, btnGrid, btnCoverFlow,  nil];
	[fakeTB setItems:toolbarItems animated:YES];
	
	[btnCancel release];
	[btnRM release];
	[flexibleSpace release];
	[btnGrid release];
	[btnCoverFlow release];
}

//Yes, we loves the landscape
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
	return YES;
}


//Simple method to let me quickly know what my orientation is. Should probably be static
-(BOOL)isLandscape
{
	UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
	if (orientation == UIDeviceOrientationLandscapeLeft  || orientation == UIDeviceOrientationLandscapeRight ) 
	{
		return YES;
	}
	else if (orientation == UIDeviceOrientationPortrait  || orientation == UIDeviceOrientationPortraitUpsideDown ) 
	{
		return NO;
	}
}


//When flipped, handles the jumping to a different orientation for a grid view really.
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	////NSLog(@"ItinDetails willrotate");
	
	if (toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight)
	{
		//[self resetForLandscape];
		if (viewType == kVIEW_TYPE_GRID)
			[self loadThumbs:5];
		//[self buttonCoverFlow:self];
		
	}
	else 
	{
		//[self resetForPortrait];
		if (viewType == kVIEW_TYPE_GRID)
			[self loadThumbs:3];
	}
	//[tableView reloadData];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload 
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc 
{
	[scroller release];
	[images release];
	[imagePos release];
	[tableView release];
	[imageData release];
	[parentMVC release];
	[imageName release];
	
	[ivBack release];
	[lblBack release];
	[titleLabel release];

	[fakeTB release];
    [super dealloc];
}

#pragma mark -
#pragma mark Loading Methods (Grid and Thumbs)

-(void) sortReceipts
{
	[imagePos removeAllObjects];
	NSMutableArray *ad = [[NSMutableArray alloc] init];
	
	[imageData removeAllObjects];
	for(NSString *imgName in rootViewController.receiptData.receipts)
	{
		ReceiptImageMetaData *rimd = [rootViewController.receiptData.receipts objectForKey:imgName];
		if([rimd.userID isEqualToString:rootViewController.settings.userName])
			[ad addObject:[rootViewController.receiptData.receipts objectForKey:imgName]];
	}
	
	NSString *sortKey;
	if(sortType == kSORT_TYPE_DATE)
		sortKey = @"dateModified";
	else 
		sortKey = @"imageName";
	
	NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:sortKey ascending:sortDirection];
	[ad sortUsingDescriptors:[NSArray arrayWithObject:aSortDescriptor]];
	
	for(int y = 0; y < [ad count]; y++)
	{
		ReceiptImageMetaData *d = [ad objectAtIndex:y];
		NSString *imgName = d.imageName;
		[imagePos insertObject:d atIndex:y];
		
		NSString *fileName = d.coverFlowName;
		UIImage *img = [[UIImage alloc] initWithData:[self getImageData:fileName]];
		if(img != nil)
		{
			[imageData addObject:img];
			[img release];
			img = nil;
		}
	}
	
	[aSortDescriptor release];
	[ad release];
}


//Loads up the table view, hides the image graph view
-(void)loadTableView
{	
	if(isPickerMode)
	{
		tableView.frame = CGRectMake(0, fakeTB.frame.size.height + 1, self.view.frame.size.width, self.view.frame.size.height - fakeTB.frame.size.height);
		tableView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	}
	
	[imageData removeAllObjects];
	[imagePos removeAllObjects];
	[self sortReceipts];
	[tableView reloadData];	
}



//blows away everything dumped into the scroller
-(void)removeImageThumbs
{
	for (UIView *view in [scroller subviews]) 
	{
		[view removeFromSuperview];
	}
}


//special method that loads up all of the thumbs for grid view
-(void)loadThumbs:(int)columns
{
	int col = 0;
	int row = 0;
	int x = 4;
	int y = 4;
	if (imagePos == nil) 
	{
		imagePos = [[NSMutableArray alloc] init];
	}
	else {
		[imagePos removeAllObjects];
	}

	int iCount = 0;
	
	[imageData removeAllObjects];
	[self sortReceipts];
	
	for (UIImageView *iView in [scroller subviews]) 
	{ 
			[iView removeFromSuperview];
	}
	
	for(int i = 0; i < [imagePos count]; i++)
	{
		ReceiptImageMetaData *rimd = [imagePos objectAtIndex:i];
		
		if (rimd != nil)
		{
			NSString *fileName = rimd.coverFlowName;
			UIImage *img;// = [[UIImage alloc] initWithData:[self getImageData:fileName]];
			//[imageData addObject:img];
			
			fileName = rimd.thumbName;
			//[img release];
			CGRect btnRect = CGRectMake(x + (col * kTHUMB_GAPPED), y + (row * kTHUMB_GAPPED), kTHUMB_SIZE, kTHUMB_SIZE);
			CGRect backRect = CGRectMake(x + (col * kTHUMB_GAPPED)- 2, y + (row * kTHUMB_GAPPED) - 2, kTHUMB_SIZE + 4, kTHUMB_SIZE + 4);
			UILabel *lblBack = [[UILabel alloc] initWithFrame:backRect];
			[lblBack setBackgroundColor:[UIColor blackColor]];	
			lblBack.alpha = 0.25;
			[scroller addSubview:lblBack];
			[lblBack release];
			
			CGRect backRect2 = CGRectMake(x + (col * kTHUMB_GAPPED)- 1, y + (row * kTHUMB_GAPPED) - 1, kTHUMB_SIZE + 2, kTHUMB_SIZE + 2);
			lblBack = [[UILabel alloc] initWithFrame:backRect2];
			[lblBack setBackgroundColor:[UIColor whiteColor]];	
			lblBack.alpha = 0.80;
			[scroller addSubview:lblBack];
			[lblBack release];
			
			
			img = [[UIImage alloc] initWithData:[self getImageData:fileName]];
			
			UIButton * button = [UIButton buttonWithType:UIButtonTypeCustom];
			button.frame =  btnRect;
			[button setImage:img forState:UIControlStateNormal];
			[button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
			button.tag = iCount; 
			[scroller addSubview:button];
			
			NSString *expName = rimd.receiptName;
			NSString *annotation = rimd.receiptAnnotation;
			
			if(expName == nil)
				expName = rimd.imageName;
			
			if(annotation == nil)
				annotation = @"";
			
			NSString *lblText = [NSString stringWithFormat:@"%@\n%@", expName, [DateTimeFormatter formatDateMediumByDate:rimd.dateModified]];
			int width = kTHUMB_SIZE -2;
			CGFloat height = [FormatUtils getTextFieldHeight:width Text:lblText FontSize:8.0f];
			
			UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(x + (col * kTHUMB_GAPPED) + 1, y + (row * kTHUMB_GAPPED) + kTHUMB_SIZE - (height +1), width, height)];
			[lbl setBackgroundColor:[UIColor whiteColor]];	
			lbl.alpha = 0.50;
			[scroller addSubview:lbl];
			[lbl release];
			
			lbl = [[UILabel alloc] initWithFrame:CGRectMake(x + (col * kTHUMB_GAPPED) + 1, y + (row * kTHUMB_GAPPED) + kTHUMB_SIZE - (height +1), width, height)];
			lbl.font = [UIFont systemFontOfSize:8];
			[lbl setBackgroundColor:[UIColor clearColor]];
			[lbl setTextAlignment:UITextAlignmentCenter];
			[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
			[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
			lbl.lineBreakMode = UILineBreakModeWordWrap;
			lbl.numberOfLines = 8;
			lbl.tag = 901;
			lbl.text = lblText;
			[scroller addSubview:lbl];
			[lbl setTextColor:[UIColor blackColor]];
			[lbl setHighlightedTextColor:[UIColor whiteColor]];
			[lbl release];
			

			
			[img release];
			img = nil;
			
			iCount++;
			
			if(col == columns)
				row++;
			
			col++;
			
			if (col > columns) {
				col = 0;
			}
			
		}
	}
	
//	for(NSString *imgName in rootViewController.receiptData.receipts)
//	{
//		ReceiptImageMetaData *rimd = [rootViewController.receiptData.receipts objectForKey:imgName];
//		
//		if (rimd != nil)
//		{
//			[imagePos addObject:rimd];
//			//NSLog(@"CFName=%@", rimd.coverFlowName);
//			NSString *fileName = rimd.coverFlowName;
//			UIImage *img = [[UIImage alloc] initWithData:[self getImageData:fileName]];
//			[imageData addObject:img];
//			
//			fileName = rimd.thumbName;
//			[img release];
//			
//			img = [[UIImage alloc] initWithData:[self getImageData:fileName]];
//			
//			UIButton * button = [UIButton buttonWithType:UIButtonTypeCustom];
//			button.frame =  CGRectMake(x + (col * kTHUMB_GAPPED), y + (row * kTHUMB_GAPPED), kTHUMB_SIZE, kTHUMB_SIZE);
//			[button setImage:img forState:UIControlStateNormal];
//			[button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
//			button.tag = iCount; 
//			[scroller addSubview:button];
//			
//			[img release];
//			
//			iCount++;
//			
//			if(col == columns)
//				row++;
//			
//			col++;
//			
//			if (col > columns) {
//				col = 0;
//			}
//			
//		}
//	}
	
	if ((row * kTHUMB_GAPPED) > 375) {
		scroller.contentSize =  CGSizeMake(320, (row * kTHUMB_GAPPED) + 20);
	}
}



#pragma mark -
#pragma mark ToolBar Button Methods
//loads up up the toolbar buttons for a table. 
-(void)loadTableViewButtons
{
	//toolbars for this view
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnGrid = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_grid.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonGridView:)];
	UIBarButtonItem *btnCoverFlow = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonCoverFlow:)];
	UIBarButtonItem *btnAlpha;
	if(sortType == kSORT_TYPE_DATE)
	{
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_alphasort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)];
	}
	else {
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_datesort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)]; 
	}
	
	UIBarButtonItem *btnDir;
	if(sortDirection == kSORT_DIRECTION_ASC)
	{
		 btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_down.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}
	else {
		btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_up.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}

	UIBarButtonItem *btnCamera = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera target:self action:@selector(buttonCameraPressed:)];
	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddFromPhotoAlbumPressed:)];
	
	if(isPickerMode)
	{
		NSArray *items = [NSArray arrayWithObjects: btnGrid, btnCoverFlow, flexibleSpace, btnAlpha, btnDir, flexibleSpace, btnAdd, flexibleSpace, btnCamera, nil];
		
		[self setToolbarItems:items animated:YES];		
	}
	else 
	{
		NSArray *items = [NSArray arrayWithObjects: btnGrid, btnCoverFlow, flexibleSpace, btnAlpha, btnDir, flexibleSpace, btnAdd, flexibleSpace, btnCamera, nil];
		
		[self setToolbarItems:items animated:YES];
	}
	
	[flexibleSpace release];
	[btnGrid release];
	[btnCoverFlow release];
	[btnAlpha release];
	[btnDir release];
	[btnAdd release];
	[btnCamera release];
}



-(void) loadGridViewButtons:(id)sender
{
	//toolbars for this view
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnTable = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_table.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonTableView:)];
	UIBarButtonItem *btnCoverFlow = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonCoverFlow:)];
	
	UIBarButtonItem *btnAlpha;
	if(sortType == kSORT_TYPE_DATE)
	{
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_alphasort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)];
	}
	else {
		btnAlpha = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_datesort.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortType:)]; 
	}
	
	UIBarButtonItem *btnDir;
	if(sortDirection == kSORT_DIRECTION_ASC)
	{
		btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_down.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}
	else {
		btnDir = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_up.png"]  style:UIBarButtonItemStylePlain target:self action:@selector(buttonSortDirection:)];
	}
	
	UIBarButtonItem *btnCamera = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera target:self action:@selector(buttonCameraPressed:)];
	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddFromPhotoAlbumPressed:)];
	
	NSArray *items = [NSArray arrayWithObjects: btnTable, btnCoverFlow, flexibleSpace, btnAlpha, btnDir, flexibleSpace, btnAdd, flexibleSpace, btnCamera, nil];
	
	[self setToolbarItems:items animated:YES];
	
	[flexibleSpace release];
	[btnTable release];
	[btnCoverFlow release];
	[btnAdd release];
	[btnCamera release];
	[btnDir release];
	[btnAlpha release];
}


-(void)buttonCameraPressed:(id)sender
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		return;
	
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	imgPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
	[UnifiedImagePicker sharedInstance].delegate = self;
	imgPicker.allowsEditing = YES;
	[self presentModalViewController:imgPicker animated:YES];

}

//switches to coverflow
- (IBAction)buttonCoverFlow:(id)sender
{
	viewTypeHold = viewType;
	viewType = kVIEW_TYPE_CFLOW;
	FlowCoverViewController *c;
	NSArray *array = [[NSBundle mainBundle] loadNibNamed:@"TestFC" owner:self options:nil];
	c = [array objectAtIndex:0];
	c.imageData = self.imageData;
	coverFlowImageIndex = -1;
	c.coverFlowImageIndex = coverFlowImageIndex;
	c.rm = self;
	[self presentModalViewController:c animated:YES];
}



//switches to table view
-(void)buttonTableView:(id)sender
{
	viewType = kVIEW_TYPE_TABLE;
	[self removeImageThumbs];
	[scroller setHidden:YES];
	[self loadTableView];
	[tableView setHidden:NO];
	
	//toolbars for this view
	if(isPickerMode)
		[self makePickerTableButtons];
	else 
		[self loadTableViewButtons];
}


//switches to grid view
-(void)buttonGridView:(id)sender
{
	viewType = kVIEW_TYPE_GRID;
	[self removeImageThumbs];
	if ([self isLandscape]) 
		[self loadThumbs:5];
	else
		[self loadThumbs:3];
	
	[scroller setHidden:NO];
	
	[tableView setHidden:YES];
	
	if(isPickerMode)
		[self makePickerGridButtons];
	else 
		[self loadGridViewButtons:self];
}



//dumps you into the receipt manager view, so you can edit the image
- (IBAction)buttonClicked:(id)sender 
{
	UIButton *button = (UIButton *)sender;
	int iTag = button.tag;
	ReceiptImageMetaData *rimd = [imagePos objectAtIndex:iTag];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rimd, @"IMAGE_DICTIONARY", nil];
	if(isPickerMode & rimd != nil)
	{
		self.imageName = rimd.imageName;
		[self buttonSelectModal:self];
	}
	else 
		[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_IMAGES ParameterBag:pBag];
	[pBag release];
	//UIImage *selectedImage = [_images objectAtIndex:button.tag];
	// Do something with image!
}


//resets the table view to be the next sorting type
-(void)buttonSortType:(id)sender
{
	if (sortType == kSORT_TYPE_DATE) {
		sortType = kSORT_TYPE_NAME;
	}
	else {
		sortType = kSORT_TYPE_DATE;
	}
	
	if(viewType == kVIEW_TYPE_GRID)
	{
		[self buttonGridView:self];
		//[self loadGridViewButtons:self];
	}
	else if(viewType == kVIEW_TYPE_TABLE)
	{
		[self loadTableView];
		[self loadTableViewButtons];
	}	
}


-(void)buttonAddFromPhotoAlbumPressed:(id)sender
{
	UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	imgPicker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
	[UnifiedImagePicker sharedInstance].delegate = self;
	imgPicker.allowsEditing = YES;
	[self presentModalViewController:imgPicker animated:YES];
}


//resets the table view to be the next sorting direction
-(void)buttonSortDirection:(id)sender
{
	if (sortDirection == YES) {
		sortDirection = NO; //kSORT_DIRECTION_DESC;
	}
	else {
		sortDirection = YES; //kSORT_DIRECTION_ASC;
	}
	
	if(viewType == kVIEW_TYPE_GRID)
	{
		[self buttonGridView:self];
		//[self loadGridViewButtons:self];
	}
	else if(viewType == kVIEW_TYPE_TABLE)
	{
		[self loadTableView];
		[self loadTableViewButtons];
	}
}


- (void)buttonCancelModal:(id)sender
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];	
}


//used when selecting the image for out of pocket expense
- (void)buttonSelectModal:(id)sender
{
	UIImage *image = [[UIImage alloc] initWithData:[self getImageData:imageName]];
	NSMutableDictionary *myBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:image, @"IMAGE", imageName, @"IMAGE_NAME", nil];
	[parentMVC setParentReturnValues:myBag];
	[image release];
	image = nil;
	[myBag release];
	[self.parentViewController dismissModalViewControllerAnimated:YES];	
}

#pragma mark -
#pragma mark Utility Methods
//returns the data for an image file name
-(NSData *)getImageData:(NSString *) fileName
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	if (paths == nil || [paths count] <=0)
		return nil;
	NSString *documentsDirectory = [paths objectAtIndex:0];
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"ReceiptFolder.getImageData documentsDirectory - %@, filename - %@", documentsDirectory, fileName] Level:MC_LOG_DEBU];

	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:fileName];
	//NSLog(@"Load Image Path: %@", initFilePath);
	return[NSData dataWithContentsOfFile:initFilePath];
}




#pragma mark -
#pragma mark Table View Data Source Methods
//- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
//{
//    return [sectionData count];
//    
//}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [imagePos count];
}

#define kCELL_HEIGHT 70
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
	ReceiptImageMetaData *rimd = [imagePos objectAtIndex:row];
	
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"BreezeData"];
	if (cell == nil) 
	{
		cell = [[[UITableViewCell alloc] initWithFrame:CGRectZero reuseIdentifier:@"BreezeData"] autorelease];
	}
	
	if (rimd != nil)
	{
		[imagePos addObject:rimd];
		
		NSString *fileName = rimd.thumbName;
		
		for (UIImageView *iView in [cell.contentView subviews]) 
		{
			if (iView.tag >= 900) 
				[iView removeFromSuperview];
		}
		
		UIImage *img = [[UIImage alloc] initWithData:[self getImageData:fileName]];
		UIImageView *imgView = [[UIImageView alloc] initWithImage:img];
		imgView.frame = CGRectMake(0, 0, 60, 60);
		imgView.tag = 900;
		[cell.contentView addSubview:imgView];

		[img release];
		img = nil;
		//imgView.image = nil;
		[imgView release];
		//imgView = nil;
		
	
		UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(64, 0, 240, 60)];
		lbl.font = [UIFont boldSystemFontOfSize:14];
		
		NSString *meKey = @"";
		for (NSString *key in rimd.meKeys) 
		{
			meKey = [rimd.meKeys objectForKey:key];
		}
		
		NSString *expName = rimd.receiptName;
		NSString *annotation = rimd.receiptAnnotation;
		
		if(expName == nil)
			expName = rimd.imageName;
		
		if(annotation == nil)
			annotation = @"";
		lbl.text = [NSString stringWithFormat:@"%@\n%@\n%@", expName, [DateTimeFormatter formatDateTimeMediumByDate:rimd.dateModified], annotation];
		lbl.lineBreakMode = UILineBreakModeWordWrap;
		lbl.numberOfLines = 3;
		lbl.tag = 901;
		[cell.contentView addSubview:lbl];
		[lbl setTextColor:[UIColor blackColor]];
		[lbl setHighlightedTextColor:[UIColor whiteColor]];
		[lbl release];
		
		[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	}
	
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
//{	
//	if(section == 0)
//		return @"Card Charges";
//	else
//		return @"Expense Reports";
//}


//- (NSInteger)tableView:(UITableView *)tableView 
//sectionForSectionIndexTitle:(NSString *)title 
//               atIndex:(NSInteger)index
//{
////    NSString *key = [keys objectAtIndex:index];
////    if (key == UITableViewIndexSearch)
////    {
////        [tableView setContentOffset:CGPointZero animated:NO];
////        return NSNotFound;
////    }
////    else return index;
//    
//}


//-(NSIndexPath *)tableView:(UITableView *)tableView 
//willSelectRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return indexPath; 
//}


//- (void)tableView:(UITableView *)tableView 
//accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
//{
////	UIButton *infoButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
////	infoButton.tag = 600001;
////	[rootViewController switchViews:infoButton ParameterBag:nil];
//}
//
//
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSUInteger row = [newIndexPath row];	
	
	ReceiptImageMetaData *rimd = [imagePos objectAtIndex:row];
	
	if(isPickerMode & rimd != nil)
	{
		self.imageName = rimd.imageName;
		[self buttonSelectModal:self];
	}
	else 
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rimd, @"IMAGE_DICTIONARY", nil];
		[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_IMAGES ParameterBag:pBag];
		[pBag release];
	}
}


//-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
//{
//    NSUInteger row = [newIndexPath row];
//	if(row == 0)
//	{
//		UIImagePickerController *picker = [[UIImagePickerController alloc] init];
//		picker.delegate = self;
//		
//		picker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
//		
//		[self presentModalViewController:picker animated:YES];
//		//[rootViewController switchToView:RECEIPT_MANAGER viewFrom:RECEIPT_MANAGER_SELECTOR ParameterBag:nil];
//	}
//	else if(row == 2)
//	{
//		[rootViewController switchToView:RECEIPT_MANAGER_IMAGES viewFrom:RECEIPT_MANAGER_SELECTOR ParameterBag:nil];
//	}
//}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 62;	
}


@end
