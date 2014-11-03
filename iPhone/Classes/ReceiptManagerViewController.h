//
//  ReceiptManagerViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "UnifiedImagePicker.h"

@interface ReceiptManagerViewController : MobileViewController <UIScrollViewDelegate, UnifiedImagePickerDelegate>
{
	UIImageView		*imageView;
	UIButton		*choosePhotoBtn;
	UIButton		*takePhotoBtn;
	BOOL			loadPhotoAlbum;
	NSMutableDictionary	*pBag;
	UIScrollView	*scroller;
	float				scale;
	int				originalW, originalH;
	float			aspectRatio;
	NSString		*imageName, *rptKey, *rpeKey, *comment, *thumbName, *cfName;
	NSDate			*dateModified, *dateCreated;
	UILabel		*lblMask;
	UIView		*cropView;
	UISlider	*sliderH, *sliderW;
	UIButton	*btnCropDone;
	UIActivityIndicatorView *activity;
}

@property (nonatomic, retain) NSDate *dateModified;
@property (nonatomic, retain) NSDate *dateCreated;
@property (nonatomic, retain) NSString *imageName;
@property (nonatomic, retain) NSString *rptKey;
@property (nonatomic, retain) NSString *rpeKey;
@property (nonatomic, retain) NSString *comment;
@property (nonatomic, retain) UILabel *lblMask;
@property (nonatomic, retain) IBOutlet UIImageView * imageView;
@property (nonatomic, retain) IBOutlet UIButton * choosePhotoBtn;
@property (nonatomic, retain) IBOutlet UIButton * takePhotoBtn;
@property (nonatomic, retain) NSMutableDictionary	*pBag;
@property (nonatomic, retain) IBOutlet UIScrollView	*scroller;
@property (nonatomic, retain) NSString *thumbName;

@property (nonatomic, retain) NSString *cfName;
@property (nonatomic, retain) UIView		*cropView;
@property (nonatomic, retain) UISlider	*sliderH;
@property (nonatomic, retain) UISlider	*sliderW;
@property (nonatomic, retain) UIButton	*btnCropDone;

@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *activity;

@property BOOL loadPhotoAlbum;
@property float scale;
@property int originalW;
@property int originalH;
@property float aspectRatio;

-(IBAction) getPhoto:(id) sender;
-(void)buttonLoadNewImage:(id)sender;
-(void) depositImage:(UIImage *)img;
-(void)buttonZoomIn:(id)sender;
-(void)buttonZoomOut:(id)sender;
-(void)scaleImage;
-(void)buttonCrop:(id)sender;
-(void)buttonSaveToReceiptsFolder:(id)sender;
-(void)buttonDeleteFromReceiptsFolder:(id)sender;

- (CGFloat)distanceBetweenTwoPoints:(CGPoint)fromPoint toPoint:(CGPoint)toPoint;
- (void) clearTouches;

-(BOOL)isLandscape;

-(void) setToolbarEverything;
-(void) setToolbarFromPicker;

-(void) startDrag: (UISlider *) aSlider;
-(void) endDrag: (UISlider *) aSlider;
-(void) startDragH: (UISlider *) aSlider;
-(void) endDragH: (UISlider *) aSlider;
-(void)buttonCropDone:(id)sender;
@end
