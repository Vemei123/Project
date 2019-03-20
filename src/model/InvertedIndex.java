/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.sun.org.apache.xerces.internal.util.DOMUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Yos Rio
 */
public class InvertedIndex {

    private ArrayList<Document> listOfDocument = new ArrayList<Document>();
    private ArrayList<Term> dictionary = new ArrayList<Term>();

    public InvertedIndex() {

    }

    public void addNewDocument(Document document) {
        listOfDocument.add(document);
    }

    public ArrayList<Posting> getUnsortedPostingList() {
        ArrayList<Posting> list = new ArrayList<Posting>();

        for (int i = 0; i < listOfDocument.size(); i++) {
            String[] termResult = listOfDocument.get(i).getListofTerm();
            for (int j = 0; j < termResult.length; j++) {
                Posting tempPosting = new Posting(termResult[j], listOfDocument.get(i));
                list.add(tempPosting);
            }
        }
        return list;
    }

    public ArrayList<Posting> getSortedPostingList() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        list = this.getUnsortedPostingList();
        Collections.sort(list);
        return list;
    }

    public ArrayList<Posting> getUnsortedPostingListWithTermNumber() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        for (int i = 0; i < listOfDocument.size(); i++) {
            ArrayList<Posting> postingDoc = listOfDocument.get(i).getListofPosting();
            for (int j = 0; j < postingDoc.size(); j++) {
                Posting tempPosting = postingDoc.get(j);
                list.add(tempPosting);
            }
        }
        return list;
    }

    public ArrayList<Posting> getSortedPostingListWithTermNumber() {
        ArrayList<Posting> list = new ArrayList<Posting>();
        list = this.getUnsortedPostingListWithTermNumber();
        Collections.sort(list);
        return list;
    }

    public void makeDictionaryWithTermNumber() {
        ArrayList<Posting> list = this.getSortedPostingListWithTermNumber();

        for (int i = 0; i < list.size(); i++) {
            if (getDictionary().isEmpty()) {
                Term term = new Term(list.get(i).getTerm());
                term.getPostingList().add(list.get(i));
                getDictionary().add(term);
            } else {
                Term tempTerm = new Term(list.get(i).getTerm());
                int position = Collections.binarySearch(getDictionary(), tempTerm);
                if (position < 0) {
                    tempTerm.getPostingList().add(list.get(i));
                    getDictionary().add(tempTerm);
                } else {
                    getDictionary().get(position).getPostingList().add(list.get(i));
                    Collections.sort(getDictionary().get(position).getPostingList());
                }
                Collections.sort(getDictionary());
            }
        }
    }

    public ArrayList<Posting> search(String kunci) {
        String[] q = kunci.split(" ");
        ArrayList<Posting> result = new ArrayList<>();
        for (int i = 0; i < q.length; i++) {
            String string = q[i];
            if (i == 0) {
                result = searchOneWord(q[i]);
            } else {
                ArrayList<Posting> result2 = searchOneWord(q[i]);
                result = intersection(result, result2);
            }
        }
        return result;
    }

    public ArrayList<Posting> intersection(ArrayList<Posting> p1, ArrayList<Posting> p2) {
        if (p1 == null || p2 == null) {
            return new ArrayList<>();
        }

        ArrayList<Posting> posting = new ArrayList<>();
        int index_p1 = 0;
        int index_p2 = 0;

        Posting post1 = p1.get(index_p1);
        Posting post2 = p2.get(index_p2);

        while (true) {
            if (post1.getDocument().getId() == post2.getDocument().getId()) {
                try {
                    posting.add(post1);
                    index_p1++;
                    index_p2++;
                    post1 = p1.get(index_p1);
                    post2 = p2.get(index_p2);
                } catch (Exception e) {
                    break;
                }
            } else if (post1.getDocument().getId() < post2.getDocument().getId()) {
                try {
                    index_p1++;
                    post1 = p1.get(index_p1);
                } catch (Exception e) {
                    break;
                }
            } else {
                try {
                    index_p2++;
                    post2 = p2.get(index_p2);
                } catch (Exception e) {
                    break;
                }
            }
        }
        return posting;
    }

    public ArrayList<Posting> searchOneWord(String kunci) {
        Term tempTerm = new Term(kunci);
        if (getDictionary().isEmpty()) {
            return null;
        } else {
            int positionTerm = Collections.binarySearch(dictionary, tempTerm);
            if (positionTerm < 0) {
                return null;
            } else {
                return dictionary.get(positionTerm).getPostingList();
            }
        }
    }

    public void makeDictionary() {
        ArrayList<Posting> list = this.getSortedPostingList();
        for (int i = 0; i < list.size(); i++) {
            if (getDictionary().isEmpty()) {
                Term term = new Term(list.get(i).getTerm());
                term.getPostingList().add(list.get(i));
                getDictionary().add(term);
            } else {
                Term tempTerm = new Term(list.get(i).getTerm());
                int position = Collections.binarySearch(getDictionary(), tempTerm);
                if (position < 0) {
                    tempTerm.getPostingList().add(list.get(i));
                    getDictionary().add(tempTerm);
                } else {
                    getDictionary().get(position).getPostingList().add(list.get(i));
                    Collections.sort(getDictionary().get(position).getPostingList());
                }
                Collections.sort(getDictionary());
            }
        }
    }

    public int getDocFreq(String term) {
        ArrayList<Term> result = getDictionary();
        int count = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getTerm().equals(term)) {
                count = result.get(i).getNumberOfDocument();
            }
        }
        return count;

    }

    public double getInverseDocFreq(String term) {
        double n = getDocFreq(term);
        double N = listOfDocument.size();
        return Math.log10(N / n);

    }

    public int getTermFreq(String term, int idDoc) {
        Document doc = new Document();
        doc.setId(idDoc);
        int index = Collections.binarySearch(listOfDocument, doc);
        if (index >= 0) {
            ArrayList<Posting> tempPost = getListOfDocument().get(index).getListofPosting();
            Posting post = new Posting();
            post.setTerm(term);
            int postIndex = Collections.binarySearch(tempPost, post);
            if (postIndex > 0) {
                return tempPost.get(postIndex).getNumberOfTerm();
            }
            return 0;
        }
        return 0;
    }

    public ArrayList<Posting> makeTFIDF(int idDocument) {
        Document doc = new Document();
        doc.setId(idDocument);
        int cek = Collections.binarySearch(listOfDocument, doc);
        if (cek < 0) {
            return null;
        } else {
            doc = listOfDocument.get(cek);
            ArrayList<Posting> result = doc.getListofPosting();
            Collections.sort(result);
            for (int i = 0; i < result.size(); i++) {
                double w = getTermFreq(result.get(i).getTerm(), idDocument) * getInverseDocFreq(result.get(i).getTerm());
                result.get(i).setTerm(result.get(i).getTerm());
                result.get(i).setNumberOfTerm(getTermFreq(result.get(i).getTerm(), idDocument));
                result.get(i).setWeight(w);
            }
            return result;
        }
    }
// ambil inner product dari query di setiap dokumen
    public double getInnerProduct(ArrayList<Posting> p1, ArrayList<Posting> p2) {
        double hasil = 0;
        // perulangan buat menghitung inner product dari bobot setiap dokumen
        for (int i = 0; i < p1.size(); i++) {
            //mencari ada atau tidak query dan dokumen 
            int pos = Collections.binarySearch(p2, p1.get(i));
            // jika ada lakukan perhitungan
            if (pos >= 0) {
                hasil = hasil + (p1.get(i).getWeight()
                        * p2.get(pos).getWeight());
            }
        }
        return hasil;
    }
// mencari bobot dari query
    public ArrayList<Posting> getQueryPosting(String query) {
        Document doc = new Document();
        doc.setContent(query);
        // array list yang menampung bobot dari query 
        ArrayList<Posting> queryPos = doc.getListofPosting();
        //perulangan utuk menghitung bobot query
        for (int i = 0; i < queryPos.size(); i++) {
            double w = queryPos.get(i).getNumberOfTerm()
                    * getInverseDocFreq(queryPos.get(i).getTerm());
            queryPos.get(i).setWeight(w);
        }

        return queryPos;
    }
    //  method menghitung panjang dokumennya
    public double getLengthOfPosting(ArrayList<Posting> posting) {
       
        double hslPost = 0;
       // perulangan mencari panjang dokumen 
        for (int i = 0; i < posting.size(); i++) {
         // rumus menghitung panjang dokumen 
            hslPost += Math.pow(posting.get(i).getWeight(), 2);
        }
        return Math.sqrt(hslPost);
    }
    // method menghitung cosine similarity dengan array list dokumen dan query 
    public double getCosineSimilarity(ArrayList<Posting> posting, ArrayList<Posting> posting1) {
       // membuat variabel inpro dari list dookumen dan query
        double InPro = getInnerProduct(posting, posting1);
      
        double hasil = 0;
       // rumus menghitung cosine similarity
        hasil = InPro / (getLengthOfPosting(posting) * getLengthOfPosting(posting1));
        return hasil;
    }
    // method mencari dengan menggunakan Inner product
    public ArrayList<SearchingResult> searchTFIDF(String query) {
        // menampung hasil dari dokumen dan bobot nilai
        ArrayList<SearchingResult> result = new ArrayList<SearchingResult>();
        // mengambil bobot dari query
        ArrayList<Posting> queryPost = this.getQueryPosting(query);
        // perulangan untuk mendapatkan inner product
        for (int i = 0; i < listOfDocument.size(); i++) {
          
            ArrayList<Posting> tempDoc = this.makeTFIDF(i+1);
          
            double innerProduct = this.getInnerProduct(queryPost, tempDoc);
           // menampung inner product dan dokumen 
            SearchingResult doc = new SearchingResult(innerProduct, tempDoc.get(i).getDocument());
        
            result.add(doc);
        }
       
        Collections.sort(result);
  
        Collections.reverse(result);
        return result;
    }
    // method mencari dengan menggunakan Cosine Similarity
    public ArrayList<SearchingResult> searchCosineSimilarity(String query) {
     
        ArrayList<SearchingResult> result = new ArrayList<SearchingResult>();
       
        ArrayList<Posting> queryPost = this.getQueryPosting(query);
        
      // perulangan untuk mendapatkan cosine similarity
        for (int i = 0; i < listOfDocument.size(); i++) {
          
            ArrayList<Posting> tempDoc = this.makeTFIDF(i+1);
          
            double similarity = this.getCosineSimilarity(queryPost, tempDoc);
           
            SearchingResult doc = new SearchingResult(similarity,tempDoc.get(i).getDocument());
         
            result.add(doc);
        }
      
        Collections.sort(result);
       
        Collections.reverse(result);
        return result;
    }

    public ArrayList<Document> getListOfDocument() {
        return listOfDocument;
    }

    public void setListOfDocument(ArrayList<Document> listOfDocument) {
        this.listOfDocument = listOfDocument;
    }

    public ArrayList<Term> getDictionary() {
        return dictionary;
    }

    public void setDictionary(ArrayList<Term> dictionary) {
        this.dictionary = dictionary;
    }

}
