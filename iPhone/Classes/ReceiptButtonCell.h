//
//  ReceiptButtonCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>	
//#import "OutOfPocketFormViewController.h"

@interface ReceiptButtonCell : UITableViewCell 
{
//	OutOfPocketFormViewController	*oopeForm;
	UIButton						*btnCamera/*, *btnPhotoAlbum, *btnReceiptFolder, *btnClear*/;
	UILabel							*lblUpdateReceipt;
}

//@property (assign, nonatomic) OutOfPocketFormViewController	*oopeForm;
@property (strong, nonatomic) IBOutlet UIButton				*btnCamera;
@property (strong, nonatomic) IBOutlet UILabel				*lblUpdateReceipt;

//@property (retain, nonatomic) IBOutlet UIButton				*btnPhotoAlbum;
//@property (retain, nonatomic) IBOutlet UIButton				*btnReceiptFolder;
//@property (retain, nonatomic) IBOutlet UIButton				*btnClear;

-(IBAction)buttonCameraPressed:(id)sender;
//-(IBAction)buttonAlbumPressed:(id)sender;
//-(IBAction)btnClearReceipt:(id)sender;
//-(IBAction)btnReceiptManager:(id)sender;
@end
