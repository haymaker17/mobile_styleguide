    //
//  ReceiptManagerViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptManagerViewController.h"
#import "RootViewController.h"
#import "ReceiptData.h"
#import "ImageUtil.h"


@implementation ReceiptManagerViewController

@synthesize imageView,choosePhotoBtn, takePhotoBtn, loadPhotoAlbum, pBag, scroller, scale, originalW, originalH, aspectRatio;
@synthesize imageName, rptKey, rpeKey, comment;
@synthesize dateModified, dateCreated, thumbName, lblMask;
@synthesize cropView, sliderH, sliderW, btnCropDone, cfName, activity;


#define kTHUMB_SIZE 75

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
	return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	if (toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || toInterfaceOrientation == UIInterfaceOrientationLandscapeRight)
	{
	}
	else 
	{
	}
}


-(NSString *)getViewIDKey
{
	return RECEIPT_MANAGER;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


//provides the mask for cropping an image
-(void)buttonCrop:(id)sender
{
	CGSize imgSize = imageView.image.size;
	int w = imgSize.width;
	int h = imgSize.height;
	int screenWidth = 320;
	
	if (w > screenWidth) 
	{
		float wDivResult = (float)w / screenWidth;
		aspectRatio = wDivResult;
		scale = 0;
		//ok, lets factor down the imageView width and height to maintain the proportions to fit screen size.  This is scale 1.
		int newWidth = w / wDivResult;
		int newHeight = h / wDivResult;
		int newY = 0;
		
		if (newHeight < 395) 
		{
			newY = (395 - newHeight) /2;
		}
		imageView.frame = CGRectMake(0, newY, newWidth, newHeight);
		scroller.contentSize = CGSizeMake(screenWidth, newHeight);
	}
		
	if (cropView == nil) 
	{
		lblMask = [[UILabel alloc] initWithFrame:CGRectMake(80, 100, 160, 200)];
		lblMask.backgroundColor = [UIColor whiteColor];
		lblMask.alpha = 0.25;
		
		btnCropDone = [UIButton buttonWithType:UIButtonTypeRoundedRect];
		btnCropDone.frame = CGRectMake(230, 325, 80, 23);
		//btnCropDone.buttonType = UIButtonTypeRoundedRect;
		btnCropDone.titleLabel.text = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
		[btnCropDone setTitle:[Localizer getLocalizedText:@"LABEL_DONE_BTN"] forState:UIControlStateNormal];
		btnCropDone.font = [UIFont boldSystemFontOfSize:10];
		[btnCropDone addTarget:self action:@selector(buttonCropDone:) forControlEvents:UIControlEventTouchUpInside];
		 
		UILabel *lblW = [[UILabel alloc] initWithFrame:CGRectMake(2, 325, 45, 23)];
		lblW.text = @"Width";
		UILabel *lblH = [[UILabel alloc] initWithFrame:CGRectMake(2, 350, 45, 23)];
		lblH.text = @"Height";
		lblW.font = [UIFont boldSystemFontOfSize:12];
		lblH.font = [UIFont boldSystemFontOfSize:12];
		
		cropView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 400)];
		
		sliderW = [[UISlider alloc] initWithFrame:CGRectMake(60, 325, 160, 23)];
		sliderH = [[UISlider alloc] initWithFrame:CGRectMake(60, 350, 160, 23)];	
		
		sliderW.minimumValue = 1;
		sliderW.maximumValue = 320;
		sliderW.value = 160;
		
		[sliderW addTarget:self action:@selector(startDrag:) forControlEvents:UIControlEventTouchDown];
		[sliderW addTarget:self action:@selector(endDrag:) forControlEvents:UIControlEventTouchUpInside | UIControlEventTouchUpOutside];
		
		sliderH.minimumValue = 1;
		sliderH.maximumValue = 400;
		sliderH.value = 200;
		[sliderH addTarget:self action:@selector(startDragH:) forControlEvents:UIControlEventTouchDown];
		[sliderH addTarget:self action:@selector(endDragH:) forControlEvents:UIControlEventTouchUpInside | UIControlEventTouchUpOutside];
		
		[cropView addSubview:lblMask];
		[cropView addSubview:btnCropDone];
		[cropView addSubview:sliderW];
		[cropView addSubview:sliderH];
		[cropView addSubview:lblW];
		[cropView addSubview:lblH];
		
		[self.view addSubview:cropView];
		[lblH release];
		[lblW release];
	}
	else 
	{
		[lblMask removeFromSuperview];
		[sliderH removeFromSuperview];
		[sliderW removeFromSuperview];
		[cropView removeFromSuperview];
	}
}


-(void)buttonCropDone:(id)sender
{
}


-(void) startDrag: (UISlider *) aSlider
{
	//lblMask.frame = CGRectMake(0, 0, aSlider.value, lblMask.frame.size.height);
}


-(void) endDrag: (UISlider *) aSlider
{
	int x = (320 - aSlider.value) /2;
	lblMask.frame = CGRectMake(x, lblMask.frame.origin.y, aSlider.value, lblMask.frame.size.height);
}


-(void) startDragH: (UISlider *) aSlider
{
	//lblMask.frame = CGRectMake(0, 0, lblMask.frame.size.width, aSlider.value);
}


-(void) endDragH: (UISlider *) aSlider
{
	int y = (400 - aSlider.value) / 2;
	lblMask.frame = CGRectMake(lblMask.frame.origin.x, y, lblMask.frame.size.width, aSlider.value);
}


//using the scaling that is not from the zoom, zooms in
-(void)buttonZoomIn:(id)sender
{	
	UIView *toolBox = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 260, 130)];
	toolBox.backgroundColor = [UIColor blackColor];
	toolBox.alpha = 0.33;
	toolBox.tag = 12345;
	
	[self.view addSubview:toolBox];
	[toolBox release];
}


//using the scale NOT from the scroller, zooms out
-(void)buttonZoomOut:(id)sender
{
	scale = scale - 0.5;
	if (scale < 0) 
	{
		scale = 0;
	}
	[self scaleImage];
}


//smokes the image from the managed receipts folder
-(void)buttonDeleteFromReceiptsFolder:(id)sender
{
	if (imageName != nil)
	{
		imageView.image = nil;
		
		[rootViewController.receiptData removeImageAndAllKeyAssociations:imageName];
		
		parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", nil];
		[rootViewController switchToView:RECEIPT_MANAGER_IMAGES viewFrom:RECEIPT_MANAGER ParameterBag:parameterBag];
	}
}


//Saves the current image to the Documents folder and then add/updates it to the receipt plist
-(void)buttonSaveToReceiptsFolder:(id)sender
{
	[self.view bringSubviewToFront:activity];
	[activity startAnimating];

	ReceiptImageMetaData *rimd;
	if (imageName == nil) 
	{
		rimd = [[ReceiptImageMetaData alloc] init];
		[rootViewController.receiptData saveReceiptImageMetaData:rimd];
		imageName = rimd.imageName;
		[rimd release];
	}
	else 
	{
		rimd = [rootViewController.receiptData.receipts objectForKey:imageName];
		imageName = rimd.imageName;
		thumbName = rimd.thumbName;
		cfName = rimd.coverFlowName;
		rimd.dateModified = [NSDate date];
		[rootViewController.receiptData saveReceiptImageMetaData:rimd];
	}
	
	[ImageUtil saveReceiptImageToDocumentsFolder:imageView.image ImageName:rimd.imageName ThumbName:rimd.thumbName CoverFlowName:rimd.coverFlowName];
	[activity stopAnimating];
	[parameterBag removeObjectForKey:@"IMAGE"];
	[self setToolbarEverything];
	[self.navigationController popViewControllerAnimated:YES];
}


//performs a simple resize of the image, keeping it constrained
-(void)scaleImage
{
	CGSize imgSize = imageView.image.size;
	
	int w = originalW * scale;
	int h = originalH * scale;
	
	if(scale > 0)
	{
		imageView.frame = CGRectMake(0, 0, w, h);
		scroller.contentSize = CGSizeMake(w, h);
	}
	else 
	{	int newWidth = originalW / aspectRatio;
		int newHeight = originalH / aspectRatio;
		imageView.frame = CGRectMake(0, 0, newWidth, newHeight);
		scroller.contentSize = CGSizeMake(320, newHeight);
	}
}


//when you come here from the picker, we only want a few options in the toolbar
-(void) setToolbarFromPicker
{
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_save.png"] 
																style:UIBarButtonItemStylePlain target:self action:@selector(buttonSaveToReceiptsFolder:)];
	
	NSArray *items = [NSArray arrayWithObjects: flexibleSpace, btnSave, flexibleSpace, nil]; //btnAnnotate btnZoom, 
	
	[self setToolbarItems:items animated:YES];
	
	[flexibleSpace release];
	[btnSave release];
}


//give us everything in the toolbar
-(void) setToolbarEverything
{
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	UIBarButtonItem *btnCamera = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCamera target:self action:@selector(buttonCameraPressed:)];
	UIBarButtonItem *btnPhotoAlbum = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonLoadNewImage:)];
	UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_save.png"] 
																style:UIBarButtonItemStylePlain target:self action:@selector(buttonSaveToReceiptsFolder:)];
	UIBarButtonItem *btnDelete = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(buttonDeleteFromReceiptsFolder:)];
	
	NSArray *items = [NSArray arrayWithObjects: btnDelete, flexibleSpace, btnSave, flexibleSpace, btnPhotoAlbum
					  , flexibleSpace, btnCamera, nil]; //btnAnnotate btnZoom, 
	
	[self setToolbarItems:items animated:YES];
	
	[flexibleSpace release];
	[btnCamera release];
	[btnPhotoAlbum release];
	[btnSave release];
	[btnDelete release];
}


-(UIView *) viewForZoomingInScrollView:(UIScrollView *)scrollView
{
	return imageView;
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [super viewDidLoad];

	scale = 1;
	
	if (parameterBag != nil & [parameterBag objectForKey:@"IMAGE"] != nil) 
	{	
		[self depositImage: [parameterBag objectForKey:@"IMAGE"]];
		[self setToolbarFromPicker];
	}
	else if (parameterBag != nil & [parameterBag objectForKey:@"IMAGE_DICTIONARY"] != nil) 
	{	
		ReceiptImageMetaData *rimd = [parameterBag objectForKey:@"IMAGE_DICTIONARY"];
		NSString *imgName = rimd.imageName;
		self.thumbName = rimd.thumbName;
		self.imageName = imgName;
		
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = [paths objectAtIndex:0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:imgName];
		NSData *data = [NSData dataWithContentsOfFile:initFilePath]; //on the device, no cached data is found!
		UIImage *img = [[UIImage alloc] initWithData:data];
		[self depositImage: img];
		[img release];
		img = nil;
		[self setToolbarEverything];
	}

	scroller.maximumZoomScale = 8.0;
	scroller.minimumZoomScale = 0.75;
	scroller.clipsToBounds = YES;
	scroller.delegate = self;	
}


//Something really odd, I'm popping into her eabout 10 times.  I think that it all has to do with the scroller and the image.  
//as the scroller resizes, so goes the content...
- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated];
}


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
	else 
	{
		return NO;
	}
}


-(void) depositImage:(UIImage *)img
{
	CGSize imgSize = img.size;
	int w = imgSize.width;
	int h = imgSize.height;
	originalH = h;
	originalW = w;
	int screenWidth = 320;
	
	if([self isLandscape])
		screenWidth = 480;
	
	if (w > screenWidth) 
	{
		float wDivResult = (float)w / screenWidth;
		aspectRatio = wDivResult;
		scale = 0;
		//float hDivResult = (float)h / 400;
		//ok, lets factor down the imageView width and height to maintain the proportions to fit screen size.  This is scale 1.
		int newWidth = w / wDivResult;
		int newHeight = h / wDivResult;
		imageView.frame = CGRectMake(0, 0, newWidth, newHeight);
		scroller.contentSize = CGSizeMake(screenWidth, newHeight);
	}
	else 
	{
		imageView.frame = CGRectMake(0, 0, imgSize.width, imgSize.height);
		scroller.contentSize = CGSizeMake(imgSize.width, imgSize.height);
		scale = 0;
	}
	imageView.image = nil;
	imageView.image = img;
}


- (void)didReceiveMemoryWarning 
{
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
	[pBag release];
	[scroller release];
	[rptKey release];
	[rpeKey release];
	[comment release];
	[dateModified release];
	[dateCreated release];
	[lblMask release];
	[cropView release];
	[sliderH release];
	[sliderW release];
	[btnCropDone release];
	[activity release];
	[imageView release];
	imageView.image = nil;
    [super dealloc];
}


#pragma mark -
#pragma mark Image Picker Methods
-(void)buttonCameraPressed:(id)sender
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		 return;
	
	UIImagePickerController *picker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	[UnifiedImagePicker sharedInstance].delegate = self;
	picker.sourceType = UIImagePickerControllerSourceTypeCamera;
	[self presentModalViewController: picker animated:YES];
}


//presents the image picker
-(IBAction) getPhoto:(id) sender 
{
	if((UIButton *) sender != choosePhotoBtn &&  
		![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		return;
	
	UIImagePickerController *picker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	[UnifiedImagePicker sharedInstance].delegate = self;
	
	if((UIButton *) sender == choosePhotoBtn) 
	{
		picker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
	} else {
		picker.sourceType = UIImagePickerControllerSourceTypeCamera;
	}
	
	[self presentModalViewController:picker animated:YES];
}


#pragma mark -
#pragma mark UnifiedImagePickerDelegate methods

-(void)unfiedImagePickerSelectedImage:(UIImage*)image
{
	[[[UnifiedImagePicker sharedInstance] imagePicker] dismissModalViewControllerAnimated:YES];
	
	if (imageName != nil) 
	{
		[imageName release];
		imageName = nil;
	}
	
	[self depositImage: image];
	[parameterBag setObject:image forKey:@"IMAGE"]; //act like we came from the other screen...
	[self setToolbarFromPicker];

}


//goes back to imagepicker to get an image
-(void)buttonLoadNewImage:(id)sender
{
	UIImagePickerController *picker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker]; 
	[UnifiedImagePicker sharedInstance].delegate = self;
	
	picker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
	
	[self presentModalViewController:picker animated:YES];
}

@end
